package com.example.controller;

// import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.example.common.ImportProgress;
import com.example.common.Result;
import com.example.entity.User;
import com.example.entity.UserExcel;
import com.example.mapper.UserMapper;

import jakarta.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PutMapping;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

import org.springframework.web.bind.annotation.DeleteMapping;

import java.io.IOException;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
public class UserController {

    private final UserMapper userMapper;

    // 构造函数：Spring 会自动把 UserMapper 传进来
    public UserController(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    @GetMapping("/users")
    public Result<Page<User>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String name) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(User::getName, name);
        }
        Page<User> p = userMapper.selectPage(new Page<>(page, size), wrapper);
        return Result.success(p);
    }

    @PostMapping("/users/add")
    public Result<String> add(@RequestBody User user) {
        // 添加校验 邮箱不能重复
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, user.getEmail()));
        if (count > 0) {
            return Result.error(400, "邮箱已存在", "failed");
        }

        userMapper.insert(user);
        return Result.success("success");
    }

    @PutMapping("/users/update")
    public Result<String> update(@RequestBody User user) {
        String failedInfo = "修改失败";
        // 先查一下用户存不存在
        User existing = userMapper.selectById(user.getId());
        if (existing == null) {
            return Result.error(400, failedInfo, "failed");
        }
        // 添加校验 邮箱不能重复
        Long count = userMapper.selectCount(new LambdaQueryWrapper<User>().eq(User::getEmail, user.getEmail()).ne(User::getId, user.getId()));
        if (count > 0) {
            return Result.error(400, failedInfo, "failed");
        }

        userMapper.updateById(user);
        return Result.success("success");
    }

    @DeleteMapping("/users/delete/{id}")
    public Result<String> delete(@PathVariable Long id) {
        String failedInfo = "删除失败";
        // 先查一下用户存不存在
        User existing = userMapper.selectById(id);
        if (existing == null) {
            return Result.error(400, failedInfo, "failed");
        }

        userMapper.deleteById(id);
        return Result.success("success");
    }

    @PutMapping("/users/recover/{id}")
    public Result<String> recover(@PathVariable Long id) {
        // 查一下包含已删除的
        User user = userMapper.selectByIdIncludeDeleted(id);
        if (user == null) {
            return Result.error(400, "用户不存在", "failed");
        }
        if (user.getDeleted() == 0) {
            return Result.error(400, "该用户未被删除，无需恢复", "failed");
        }
        userMapper.recoverById(id);
        return Result.success(null);
    }

    @PostMapping("/users/import")
    public Result<String> importUsers(@RequestParam("file") MultipartFile file) {
        String taskId = UUID.randomUUID().toString();  // 生成唯一任务ID

        // 异步执行导入
        String finalTaskId = taskId;
        new Thread(() -> {
            try {
                doImport(file, finalTaskId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

        return Result.success(taskId);  // 立即返回任务ID
    }

    // 实际导入逻辑，异步执行
    private void doImport(MultipartFile file, String taskId) throws Exception {
        List<UserExcel> list = EasyExcel.read(file.getInputStream())
                .head(UserExcel.class)
                .sheet()
                .doReadSync();

        ImportProgress.Progress p = new ImportProgress.Progress();
        p.setTotal(list.size());
        ImportProgress.put(taskId, p);

        for (UserExcel excel : list) {
            try {
                Long count = userMapper.selectCount(
                    new LambdaQueryWrapper<User>().eq(User::getEmail, excel.getEmail())
                );
                if (count == 0) {
                    User user = new User();
                    user.setName(excel.getName());
                    user.setAge(excel.getAge());
                    user.setEmail(excel.getEmail());
                    userMapper.insert(user);
                    p.setSuccess(p.getSuccess() + 1);
                } else {
                    p.setFailed(p.getFailed() + 1);
                }
            } catch (Exception e) {
                p.setFailed(p.getFailed() + 1);
            }
            p.setProcessed(p.getProcessed() + 1);
            Thread.sleep(500);  // 让前端能看到进度变化
        }
        p.setDone(true);
    }

    // 查询导入进度
    @GetMapping("/users/import/progress/{taskId}")
    public Result<ImportProgress.Progress> getProgress(@PathVariable String taskId) {
        ImportProgress.Progress p = ImportProgress.get(taskId);
        if (p == null) {
            return Result.error(400, "任务不存在");
        }
        return Result.success(p);
    }


    @GetMapping("/users/export")
    public void exportExcel(
            HttpServletResponse response,
            @RequestParam(required = false) String name) throws IOException {
        // 动态拼接查询条件
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (name != null && !name.isEmpty()) {
            wrapper.like(User::getName, name);
        }
        // 查询所有用户
        List<User> userList = userMapper.selectList(wrapper);
        // User 转 UserExcel，只导出需要的字段
        List<UserExcel> excelList = userList.stream().map(u -> {
            UserExcel e = new UserExcel();
            e.setId(u.getId());
            e.setName(u.getName());
            e.setAge(u.getAge());
            e.setEmail(u.getEmail());
            return e;
        }).collect(Collectors.toList());
        // 设置响应头，告诉浏览器这是一个 Excel 文件
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 文件名加时间戳
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String fileName = URLEncoder.encode("用户数据_" + timestamp, "UTF-8").replace("+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        // 写入 Excel
        EasyExcel.write(response.getOutputStream(), UserExcel.class)
                .sheet("用户列表")
                .doWrite(excelList);
    }

    @GetMapping("/users/export/template")
    public void downloadTemplate(HttpServletResponse response) throws IOException {
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("导入模板", "UTF-8").replace("+", "%20");
        response.setHeader("Content-disposition", "attachment;filename=" + fileName + ".xlsx");

        // 生成一个空模板，只有表头，没有数据
        EasyExcel.write(response.getOutputStream(), UserExcel.class)
                .sheet("导入用户")
                .doWrite(List.of());  // 空列表，只写表头
    }

}
