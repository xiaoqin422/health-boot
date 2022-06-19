package com.project.simbot.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.text.UnicodeUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.project.simbot.dao.RoleDao;
import com.project.simbot.dao.TaskHealthDao;
import com.project.simbot.entity.TaskHealth;
import com.project.simbot.service.TaskHealthService;
import com.project.simbot.util.SendMessageUtil;
import lombok.extern.slf4j.Slf4j;
import love.forte.common.ioc.annotation.Beans;
import love.forte.common.ioc.annotation.Depend;
import love.forte.simbot.api.sender.Sender;
import love.forte.simbot.bot.BotManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 包名: com.project.simbot.service.impl
 * 类名: TaskServiceImpl
 * 创建用户: 25789
 * 创建日期: 2022年03月01日 1:28
 * 项目名: simbot-mirai-health
 *
 * @author: 秦笑笑
 **/
@Beans
@Slf4j
public class TaskHealthServiceImpl implements TaskHealthService {
    private static final String url = "http://yx.ty-ke.com/Home/Monitor/monitor_add";
    private static final String regex = "(?<province>[^省]+省|.+自治区)(?<city>[^自治州]+自治州|[^市]+市|[^盟]+盟|[^地区]+地区|.+区划)(?<county>[^市]+市|[^县]+县|[^旗]+旗|.+区)?(?<town>[^区]+区|.+镇)?(?<village>.*)";
    private static final Map param = new HashMap<>();
    @Depend
    private TaskHealthDao taskHealthDao;
    @Depend
    private BotManager botManager;
    @Depend
    private RoleDao roleDao;

    static {
        param.put("jk_type", "健康");
        param.put("wc_type", "否");
        param.put("jc_type", "否");
        param.put("is_verify", "0");
    }

    @Override
    public String creatTask(String accountCode, String address, String pid) {
        if (StrUtil.isBlank(accountCode) || StrUtil.isBlank(address) || StrUtil.isBlank(pid)) {
            return "信息缺失，创建失败！";
        }
        if (taskHealthDao.countOfPid(pid) >= 1) {
            return "该身份证已被绑定";
        }
        TaskHealth taskHealth = taskHealthDao.searchTaskByAccountCode(accountCode);
        if (taskHealth == null) {
            taskHealth = new TaskHealth();
            taskHealth.setAddress(address);
            taskHealth.setAccountCode(accountCode);
            taskHealth.setPid(pid);
            taskHealthDao.insert(taskHealth);
        } else if ("-1".equals(taskHealth.getStatus())) {
            taskHealth.setStatus("1");
            taskHealth.setAddress(address);
            taskHealth.setAccountCode(accountCode);
            taskHealth.setPid(pid);
            taskHealthDao.update(taskHealth);
        } else if ("0".equals(taskHealth.getStatus())) {
            return "任务已被禁用,请联系管理员！";
        } else {
            return "打卡任务已创建，请勿重复创建！";
        }
        return "一键打卡任务绑定成功";
    }

    @Override
    public String creatAutoTask(String accountCode, String groupCode) {
        TaskHealth taskHealth = taskHealthDao.searchTaskByAccountCode(accountCode);
        if (taskHealth == null || "-1".equals(taskHealth.getStatus())) {
            return "尚未绑定任务，请先进行任务绑定！";
        } else if ("0".equals(taskHealth.getStatus())) {
            return "任务已被禁用,请联系管理员！";
        } else {
            taskHealth.setGroupCode(groupCode);
            taskHealth.setStatus("2");
            taskHealthDao.update(taskHealth);
            return "已加入定时任务";
        }
    }

    @Override
    public String deleteAutoTask(String accountCode) {
        TaskHealth taskHealth = taskHealthDao.searchTaskByAccountCode(accountCode);
        if (taskHealth == null || "-1".equals(taskHealth.getStatus())) {
            return "尚未绑定任务，请先进行任务绑定！";
        } else if ("0".equals(taskHealth.getStatus())) {
            return "任务已被禁用,请联系管理员！";
        } else {
            taskHealthDao.updateTaskStatus(accountCode, "1");
            return "已取消定时任务";
        }
    }


    /**
     * 执行一键打卡  1：执行完成  -1：任务不存在 0：任务异常
     *
     * @param accountCode 用户QQ号码
     * @return 执行结果
     */
    @Override
    public int doTask(String accountCode) {
        TaskHealth taskHealth = taskHealthDao.searchTaskByAccountCode(accountCode);
        if (taskHealth == null || "-1".equals(taskHealth.getStatus())) {
            return -1;
        } else if ("0".equals(taskHealth.getStatus())) {
            return 0;
        } else {
            String s = healthPost(taskHealth.getAddress(), taskHealth.getPid());
            taskHealth.setLog(s);
            taskHealthDao.update(taskHealth);
        }
        return 1;
    }

    @Override
    public String healthPost(String address, String pid) {
        final String regex_pid = "/(\\d{6})\\d*([0-9a-zA-Z]{6})/";
        String resultMsg;
        HttpRequest request = HttpUtil.createPost(url);
        request.contentType("multipart/form-data;charset=UTF-8");
        // 身份证号
        param.put("mobile", pid);
        // 体温
        String tw = String.valueOf(RandomUtil.randomDouble(35.5, 36.8, 1, null));
        param.put("title", tw);
        // 位置
        param.put("address", address);
        param.put("province", ReUtil.get(regex, address, "province"));
        param.put("city", ReUtil.get(regex, address, "city"));
        param.put("district", ReUtil.get(regex, address, "county"));
        request.form(param);
        HttpResponse response = request.execute();
        String time = DateUtil.now();
        JSONObject result;
        try {
            result = (JSONObject) JSON.parse(response.body());
        } catch (Exception e) {
            return pid + "|" + time + "|" + "打卡失败|" + "系统发生错误! 身份证信息未经系统认证！";
        }
        String code = (String) result.get("code");
        String msg = UnicodeUtil.toString((String) result.get("msg"));
        if (code.equals("200") || (code.equals("400") && msg.equals("您已提交当前时段数据！请勿重复提交！"))) {
            resultMsg = pid.substring(0, 6) + "****" + pid.substring(14) + "|" + time + "|" + tw + "|" + msg;
            log.info(resultMsg);
        } else {
            resultMsg = pid + "|" + time + "|" + "打卡失败|" + msg;
            log.error(resultMsg);
        }
        return resultMsg;
    }

    @Override
    public String updateTaskStatus(String accountCode, String status) {
        TaskHealth taskHealth = taskHealthDao.searchTaskByAccountCode(accountCode);
        if (taskHealth == null || taskHealth.getStatus().equals("-1")) {
            return "该账户未绑定任务,请先进行任务绑定！";
        }
        taskHealthDao.updateTaskStatus(accountCode, status);
        return "修改成功";
    }

    @Override
    public String searchTaskLog(String accountCode) {
        return taskHealthDao.searchTaskLogByAccountCode(accountCode);
    }

    @Override
    public String updateTaskAddress(String accountCode, String address) {
        TaskHealth taskHealth = taskHealthDao.searchTaskByAccountCode(accountCode);
        if (taskHealth == null || taskHealth.getStatus().equals("-1")) {
            return "该账户未绑定任务,请先进行任务绑定！";
        }
        taskHealth.setAddress(address);
        return taskHealthDao.update(taskHealth) == 1 ? "修改成功" : "修改失败";
    }

    @Override
    public void autoTask() {
        List<TaskHealth> taskHealths = taskHealthDao.selectAll();
        Sender sender = botManager.getDefaultBot().getSender().SENDER;
        for (TaskHealth health : taskHealths) {
            if ("2".equals(health.getStatus())) {
                String s = healthPost(health.getAddress(), health.getPid());
                health.setLog(s);
                taskHealthDao.update(health);
                if (StrUtil.isBlank(health.getGroupCode())) {
                    sender.sendPrivateMsg(health.getAccountCode(), SendMessageUtil.getHealthTaskMessageLog(s).toString());
                } else {
                    try {
                        sender.sendPrivateMsgAsync(health.getAccountCode(), health.getGroupCode(), SendMessageUtil.getHealthTaskMessageLog(s).toString());
                    } catch (Exception e) {
                        log.error(e.toString());
                    }
                }

            }
        }
    }

    @Override
    public TaskHealth searchTask(String accountCode) {
        return taskHealthDao.searchTaskByAccountCode(accountCode);
    }

    @Override
    public List<TaskHealth> searchAllTask() {
        return taskHealthDao.selectAll();
    }


}