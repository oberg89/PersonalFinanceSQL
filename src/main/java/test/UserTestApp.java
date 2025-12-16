package test;

import repository.JdbcUserRepository;

public class UserTestApp {
    public static void main(String[] args) {
        JdbcUserRepository repo = new JdbcUserRepository();

        var registered = repo.registerUser("teacher", "S3cretPass!");
        System.out.println("Register returned: " + registered);

        var auth = repo.authenticate("teacher", "S3cretPass!");
        System.out.println("Authenticate returned: " + auth);
    }
}
