import com.example.account_service.exception.AccountNotFoundException;
import com.example.account_service.model.Account;
import com.example.account_service.repository.AccountRepository;
import com.example.account_service.service.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Service
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final KafkaTemplate<String, Account> kafkaTemplate;
    private static final String ACCOUNT_TOPIC = "account-events";

    @Autowired
    public AccountServiceImpl(AccountRepository accountRepository, KafkaTemplate<String, Account> kafkaTemplate) {
        this.accountRepository = accountRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Override
    public Account createAccount(Account account) {
        Account savedAccount = accountRepository.save(account);
        kafkaTemplate.send(ACCOUNT_TOPIC, savedAccount);  // Публикуем событие
        return savedAccount;
    }

    @Override
    public Optional<Account> getAccountById(Long accountId) {
        return accountRepository.findById(accountId);
    }

    @Override
    public Account updateAccountBalance(Long accountId, BigDecimal amount) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException("Account not found"));
        account.setBalance(account.getBalance().add(amount));
        Account updatedAccount = accountRepository.save(account);
        kafkaTemplate.send(ACCOUNT_TOPIC, updatedAccount);  // Публикуем обновление
        return updatedAccount;
    }

    @Override
    public void deleteAccount(Long accountId) {
        accountRepository.deleteById(accountId);
        kafkaTemplate.send(ACCOUNT_TOPIC, new Account(accountId));  // Отправляем удаление
    }

    @Override
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }
}
