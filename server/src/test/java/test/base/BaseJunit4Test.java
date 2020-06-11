package test.base;


import edp.DavinciServerApplication;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = {DavinciServerApplication.class})// 指定启动类
public class BaseJunit4Test {
}
