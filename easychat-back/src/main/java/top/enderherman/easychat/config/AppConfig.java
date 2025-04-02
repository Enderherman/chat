package top.enderherman.easychat.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;



@Data
@Component("appConfig")
public class AppConfig {

    /**
     * webSocket端口
     */
    @Value("${ws.port:}")
    private Integer wsPost;

    /**
     * 文件目录
     */
    @Value("${project.folder:}")
    private String projectFolder;

    /**
     * 管理员
     */
    @Value("${admin.emails:}")
    private String adminEmails;
}
