package online.faramita.bbs.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import online.faramita.bbs.config.GithubConfig;

@Component
public class GithubFileUtil {

    @Autowired
    private GithubConfig Param;

    public String upload(MultipartFile file) throws IOException {
        // 获取上传文件的输入流
        InputStream inputStream = file.getInputStream();
        // 文件转换为base64
        byte[] fileBytes = inputStream.readAllBytes();
        String fileBase64 = Base64.getEncoder().encodeToString(fileBytes);

        // 生成UUID文件名
        String originalFilename = file.getOriginalFilename();
        String fileName = UUID.randomUUID().toString() + originalFilename.substring(originalFilename.lastIndexOf("."));

        // 上传文件到Github
        JSONObject param = new JSONObject();
        param.put("message", "");
        param.put("content", fileBase64);
        param.put("branch", Param.branch);

        JSONObject committer = new JSONObject();
        committer.put("name", Param.name);
        committer.put("email", Param.email);
        param.put("committer", committer);

        // 上传URL
        String url = "https://api.github.com/repos/" + Param.owner + "/" + Param.repo + "/contents/" + Param.path + fileName; 

        // 发起PUT请求上传文件
        HttpResponse response = HttpRequest.put(url)
                .header("Accept", "application/vnd.github+json")
                .header("Authorization", Param.getAuthorization())
                .body(param.toJSONString())
                .execute();

        // 解析返回结果获取下载链接
        JSONObject jsonObject = JSONObject.parseObject(response.body());
        JSONObject content = jsonObject.getJSONObject("content");
        return content.getString("download_url");
    }
}
