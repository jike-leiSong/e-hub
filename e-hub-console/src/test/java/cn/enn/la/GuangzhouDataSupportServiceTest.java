package cn.sl.ehub.upstream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @Author sl
 * @Phone 18910140332
 * @Date 2026-05-28
 */
@SpringBootTest(classes = EHubConsoleApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@RunWith(SpringJUnit4ClassRunner.class)
public class GuangzhouDataSupportServiceTest {

    @Test
    public void test() {
        Integer type = 1;
        switch (type) {
            case 1:
            case 2:
                System.out.println("2222222222");
                break;
            case 3:
                System.out.println("3333333333");
                break;
        }
    }
}
