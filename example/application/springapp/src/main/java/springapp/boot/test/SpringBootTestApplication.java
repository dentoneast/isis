package springapp.boot.test;

import org.springframework.boot.autoconfigure.SpringBootApplication;

import isis.incubator.IsisBoot;
import springapp.dom.DomainModule;

@SpringBootApplication(scanBasePackageClasses= {DomainModule.class, IsisBoot.class})
public class SpringBootTestApplication {

}
