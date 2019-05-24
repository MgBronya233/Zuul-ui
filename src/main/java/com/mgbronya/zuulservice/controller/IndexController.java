package com.mgbronya.zuulservice.controller;

import com.mgbronya.zuulservice.dto.ZuulFilterDto;
import com.netflix.zuul.FilterFileManager;
import com.netflix.zuul.FilterLoader;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.filters.FilterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.ModelAndView;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@RestController
public class IndexController {

    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @GetMapping("/")
    public ModelAndView index() {
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.setViewName("index");
        return modelAndView;
    }

    @GetMapping("/filters")
    @ResponseBody
    public ResponseEntity<List<ZuulFilterDto>> getActiveFilters() {
        FilterRegistry filterRegistry = FilterRegistry.instance();
        List<ZuulFilter> filters = new ArrayList(filterRegistry.getAllFilters());

        List<ZuulFilterDto> resultFilters = new ArrayList<>();
        for (ZuulFilter filter : filters) {
            if (filter.getClass().getPackage().toString().startsWith("package filters")) {
                ZuulFilterDto zuulFilterDto = new ZuulFilterDto();
                zuulFilterDto.setFilterName(filter.getClass().getSimpleName() + ".groovy");
                zuulFilterDto.setFilterOrder(filter.filterOrder());
                zuulFilterDto.setFilterType(filter.filterType());
                zuulFilterDto.setShouldFilter(filter.shouldFilter());
                resultFilters.add(zuulFilterDto);
            }
        }
        return ResponseEntity.ok(resultFilters);
    }

    @DeleteMapping("/filter")
    public ResponseEntity<String> downFilter(@RequestParam String filterType, @RequestParam String filterName) throws NoSuchFieldException, IllegalAccessException {
        FilterLoader filterLoader = FilterLoader.getInstance();
        FilterFileManager.getInstance();

        String fileRoot = applicationContext.getEnvironment().getProperty("zuul.filter.root")
                + File.separator + filterType.toLowerCase() + File.separator + filterName;

        FilterRegistry filterRegistry = FilterRegistry.instance();

        Field field = FilterLoader.class.getDeclaredField("hashFiltersByType");
        field.setAccessible(true);
        ConcurrentHashMap<String, List<ZuulFilter>> hashFiltersByType = (ConcurrentHashMap<String, List<ZuulFilter>>) field.get(filterLoader);

        File file = new File(fileRoot);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                filterRegistry.remove(fileRoot + filterName);
                hashFiltersByType.remove(filterType);
                return ResponseEntity.ok("删除" + filterName + "成功!");
            } else {
                return ResponseEntity.status(500).body("删除" + filterName + "失败!");
            }
        } else {
            return ResponseEntity.status(500).body("删除" + filterName + "不存在!");
        }
    }

    /**
     * 实现文件上传
     */
    @PostMapping("/fileUpload")
    @ResponseBody
    public ResponseEntity<String> fileUpload(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return ResponseEntity.status(500).body("选择文件错误!");
        }
        String fileName = file.getOriginalFilename();

        String path = applicationContext.getEnvironment().getProperty("zuul.filter.root");

        BufferedInputStream in = null;

        try {
            in = new BufferedInputStream(file.getInputStream());

            byte[] bytes = new byte[2048];
            //接受读取的内容(n就代表的相关数据，只不过是数字的形式)
            int n = -1;
            //循环取出数据
            while ((n = in.read(bytes, 0, bytes.length)) != -1) {
                //转换成字符串
                String str = new String(bytes, 0, n, "UTF-8");

                if (str.contains("return")) {
                    if (str.toLowerCase().contains("pre")) {
                        path += File.separator  + "pre";
                    }
                    if (str.toLowerCase().contains("post")) {
                        path += File.separator  + "post";
                    }
                    if (str.toLowerCase().contains("route")) {
                        path += File.separator  + "route";
                    }
                    if (str.toLowerCase().contains("error")) {
                        path += File.separator  + "error";
                    }
                    break;
                }

            }

            File dest = new File(path + File.separator + fileName);

            if (!dest.getParentFile().exists()) {
                dest.getParentFile().mkdir();
            }
            file.transferTo(dest);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                in.close();
            }
        }
        return ResponseEntity.ok("文件上传成功!");
    }

}
