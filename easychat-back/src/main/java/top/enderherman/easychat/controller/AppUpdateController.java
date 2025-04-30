package top.enderherman.easychat.controller;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import top.enderherman.easychat.annotation.GlobalInterceptor;
import top.enderherman.easychat.common.BaseResponse;
import top.enderherman.easychat.entity.po.AppUpdate;
import top.enderherman.easychat.entity.query.AppUpdateQuery;
import top.enderherman.easychat.entity.vo.AppUpdateVO;
import top.enderherman.easychat.entity.vo.PaginationResultVO;
import top.enderherman.easychat.service.AppUpdateService;
import top.enderherman.easychat.utils.StringUtils;

import javax.annotation.Resource;
import javax.validation.constraints.NotNull;
import java.io.IOException;

@RestController("appUpdateController")
@RequestMapping("/app")
public class AppUpdateController {

    @Resource
    private AppUpdateService appUpdateService;

    /**
     * 获取更新信息列表
     */
    @RequestMapping("/loadUpdateList")
    @GlobalInterceptor(checkAdmin = true)
    public BaseResponse<PaginationResultVO<AppUpdate>> loadUpdateList(AppUpdateQuery query) {
        query.setOrderBy("id desc");
        PaginationResultVO<AppUpdate> resultVO = appUpdateService.findListByPage(query);
        return BaseResponse.success(resultVO);
    }

    /**
     * 发布或者修改更新
     */
    @RequestMapping("/saveUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public BaseResponse<?> saveUpdate(Integer id,
                                      @NotNull String version,
                                      @NotNull String updateDesc,
                                      @NotNull Integer fileType,
                                      String outerLink,
                                      MultipartFile file) throws IOException {
        AppUpdate appUpdate = new AppUpdate();
        appUpdate.setId(id);
        appUpdate.setVersion(version);
        appUpdate.setUpdateDesc(updateDesc);
        appUpdate.setFileType(fileType);
        appUpdate.setOuterLink(outerLink==null?"":outerLink);
        appUpdateService.saveUpdate(appUpdate, file);
        return BaseResponse.success();
    }

    /**
     * 删除更新
     */
    @RequestMapping("/deleteUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public BaseResponse<?> deleteUpdate(@NotNull Integer id) {
        appUpdateService.deleteAppUpdateById(id);
        return BaseResponse.success();
    }

    /**
     * 发布更新
     */
    @RequestMapping("/postUpdate")
    @GlobalInterceptor(checkAdmin = true)
    public BaseResponse<?> postUpdate(@NotNull Integer id, @NotNull Integer status, String grayscaleUid) {
        appUpdateService.postUpdate(id, status, grayscaleUid);
        return BaseResponse.success();
    }

    /**
     * 检测更新
     */
    @RequestMapping("/checkUpdate")
    @GlobalInterceptor
    public BaseResponse<AppUpdateVO> checkUpdate(String version, String uid) {
        return StringUtils.isEmpty(version) ? BaseResponse.success() : BaseResponse.success(appUpdateService.getLatestUpdate(version, uid));
    }
}
