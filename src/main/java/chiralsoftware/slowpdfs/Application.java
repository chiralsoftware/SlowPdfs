package chiralsoftware.slowpdfs;

import java.util.logging.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@SpringBootApplication
@EnableAsync
public class Application extends SpringBootServletInitializer {

    private static final Logger LOG = Logger.getLogger(Application.class.getName());
    
    public static void main(String[] args) {
        LOG.info("Starting applicaiton");
        SpringApplication.run(Application.class, args);
    }
    
    @Bean
    public ThreadPoolTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(20);
        return executor;
    }    
    
}