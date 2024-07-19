package by.jenka.rss.authorizationservice.lambda.service;

import by.jenka.rss.authorizationservice.lambda.repository.UserRepository;
import lombok.Setter;

@Setter
public class UserService {

    private UserRepository userRepository = new UserRepository();

    public boolean isValidCredentials(String user, String password) {
        return userRepository.findByName(user)
                .map(details -> details.isPasswordValid(password))
                .orElse(false);
    }
}
