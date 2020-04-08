package test;

import org.mindrot.jbcrypt.BCrypt;

public class UserTest {

    public static void main(String[] args) {
        System.out.println(BCrypt.hashpw("ZY.com_168", BCrypt.gensalt()));

    }
}
