package cn.itcast.core.controller.upload;

import cn.itcast.core.fastDFS.FastDFSClient;
import cn.itcast.core.pojo.entity.Result;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/upload")
public class UploadController {
    @Value("${FILE_SERVER_URL}" )
    private String FILE_SERVER_URL;
    @RequestMapping("/uploadFile.do")
    public Result uploadFile(MultipartFile file) {
        try {
            //使用工具类将附件上传
            String conf = "classpath:fastDFS/fdfs_client.conf";
            FastDFSClient fastDFSClient = new FastDFSClient(conf);
            String filename = file.getOriginalFilename();           //获取附件文件名字
            String extName = FilenameUtils.getExtension(filename);  //获取附件文件后缀名
            String path = fastDFSClient.uploadFile(file.getBytes(), filename, null);//上传并获附件路径
            String url = FILE_SERVER_URL+ path;          //返回路径给前段用来显示图片
            return new Result(true, url);
        } catch (Exception e) {
            e.printStackTrace();
            return new Result(false, "上传失败");
        }

    }


}
