package by.jenka.rss.authorizationservice.lambda.repository;

import by.jenka.rss.authorizationservice.lambda.repository.entity.UserDetails;

import java.util.Optional;

public class UserRepository {

    public Optional<UserDetails> findByName(String name) {
        System.out.printf("Retrieve details for user : %s%n", name);
        return Optional.of(System.getenv().get(name))
                .map(pass -> new UserDetails(name, pass))
                .or(() -> {
                    System.out.println("User %s not found".formatted(name));
                    return Optional.empty();
                });
    }
}
