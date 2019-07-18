Springboot Big File Upload Demo
===============================

大文件 分片、断点续传
------------------------
- 时间点1 checkFile：所有分块进行上传之前,检查文件是否存在,如果存在,则跳过该文件，实现秒传功能
- 时间点2 checkChunk and upload：如果有分块上传，则每个分块上传之前，判断分块是否存在，存在则跳过，实现断点续传
- 时间点3 merge：分片上传完成后，通知后台合成分片


application.yml
----------------
```yaml
spring:
  resources:
    static-locations:classpath:/META-INF/resources/,classpath:/resources/,classpath:/static/,classpath:/assets/,classpath:/public/,file:${file.upload}
    
file:
  upload: /Users/leonard/uploadTest/
```

- Spring Boot的默认静态资源的路径为（优先级从从高到低）： 
`spring.resources.static-locations=classpath:/META-INF/resources/,classpath:/resources/,classpath:/static/,classpath:/public/` 

- 自定义静态资源路径的配置，追加`file:${file.upload}`，通过`http://localhost:8082/test.png`就可以访问到`/Users/leonard/uploadTest/`本地文件夹里的文件

- 下面配置生效后，之前访问路径由:`localhost:8082/test.jpg`变成了`localhost:8082/my/test.jpg`
`spring.mvc.static-path-pattern: /my/**`

[Learn More](https://www.itmsbx.com/article/17)

Run
-----
运行项目，访问[http://localhost:8082](http://localhost:8082)