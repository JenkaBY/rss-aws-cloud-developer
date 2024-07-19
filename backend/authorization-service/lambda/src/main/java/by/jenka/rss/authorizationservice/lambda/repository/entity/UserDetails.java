package by.jenka.rss.authorizationservice.lambda.repository.entity;

public record UserDetails(String name, String password) {

    public boolean isPasswordValid(String validatePassword) {
        System.out.println("Validating pass '" + validatePassword + "' against '" + password + "'");

        return password.equals(validatePassword);
    }
}
