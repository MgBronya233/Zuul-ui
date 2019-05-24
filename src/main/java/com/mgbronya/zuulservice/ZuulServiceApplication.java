package com.mgbronya.zuulservice;

import com.netflix.zuul.FilterFileManager;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.groovy.GroovyCompiler;
import com.netflix.zuul.groovy.GroovyFileFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.context.ConfigurableApplicationContext;

import javax.annotation.PostConstruct;
import java.io.File;

@EnableZuulProxy
@SpringBootApplication
public class ZuulServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ZuulServiceApplication.class, args);
    }

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @PostConstruct
    public void zuulInit() {
        FilterLoader.getInstance().setCompiler(new GroovyCompiler());

        // 读取配置，获取脚本根目录
        String scriptRoot = applicationContext.getEnvironment().getProperty("zuul.filter.root");
        // 获取刷新间隔
        String refreshInterval = System.getProperty("zuul.filter.refreshInterval", "5");
        if (scriptRoot.length() > 0) {
            scriptRoot = scriptRoot + File.separator;
        }
        try {
            FilterFileManager.setFilenameFilter(new GroovyFileFilter());
            FilterFileManager.init(Integer.parseInt(refreshInterval), scriptRoot + "pre",
                    scriptRoot + "route", scriptRoot + "post", scriptRoot + "error");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

}
