package test;

import edp.davinci.core.enums.types.DateTypeEnum;

import java.util.Arrays;

public class UserTest {

    public static void main(String[] args) {
//        System.out.println(BCrypt.hashpw("ZY.com_168", BCrypt.gensalt()));
        String[] q = Arrays.stream(DateTypeEnum.class.getEnumConstants()).map(Enum::name).toArray(String[]::new);
       }
}
