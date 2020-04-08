package edp.davinci.addons;

import com.alibaba.fastjson.JSONArray;
import com.zyframework.core.mq.MQMessage;
import com.zyframework.core.mq.MQTopicRef;
import com.zyframework.core.mq.MessageHandler;
import edp.core.exception.ServerException;
import edp.davinci.core.common.Constants;
import edp.davinci.core.enums.UserOrgRoleEnum;
import edp.davinci.core.enums.mq.UcinAccessTypeEnum;
import edp.davinci.dao.OrganizationMapper;
import edp.davinci.dao.RelUserOrganizationMapper;
import edp.davinci.dao.UserMapper;
import edp.davinci.model.Organization;
import edp.davinci.model.RelUserOrganization;
import edp.davinci.model.User;
import edp.davinci.model.mq.UcinAccess;
import edp.davinci.service.OrganizationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author linda
 */
@MQTopicRef("ucin-access")
@Component
@Slf4j
public class UcinAccessMessageHanlder implements MessageHandler {
    @Resource
    private UserMapper userMapper;
    @Resource
    private OrganizationMapper organizationMapper;
    @Resource
    private RelUserOrganizationMapper relUserOrganizationMapper;
    @Autowired
    private OrganizationService organizationService;
    private static String adminUsername = "admin";

    @Override
    public void process(MQMessage message) {
        List<UcinAccess> ucinAccesses = JSONArray.parseArray(message.getBody().toString(), UcinAccess.class);
        List<UcinAccess> filterUcinAccesses = ucinAccesses.stream().filter(u -> Constants.UCIN_APPID.equals(u.getAppId())).collect(Collectors.toList());
        if (filterUcinAccesses.size() > 0) {
            filterUcinAccesses.forEach(f -> {
                if (UcinAccessTypeEnum.ADD.name().equals(f.getType())) {
                    userAdd(f);
                } else if (UcinAccessTypeEnum.REMOVE.name().equals(f.getType())) {
                    userRemove(f);
                }
            });
        }
        log.info("消息处理完成,收到的内容:{}", message.toJSON());
    }

    /**
     * 用户新增和加入超级管理员的组织
     *
     * @param ucinAccess
     * @return
     */
    public void userAdd(UcinAccess ucinAccess) throws ServerException {
        Date date = new Date();
        //获取用户
        User user = userMapper.selectByUsername(ucinAccess.getPptName());
        //获取超级管理员账号
        User userAdmin = userMapper.selectByUsername(adminUsername);
        if (userAdmin == null) {
            log.error("ucin-access MQ Hanlder add error :admin user is null");
            return;
        }
        Organization organization = organizationMapper.getByUserId(userAdmin.getId());
        if (organization == null) {
            log.error("ucin-access MQ Hanlder add error :admin's Organization is null");
            return;
        }
        //用户账号是否已经存在
        if (user != null) {
            if (!user.getActive()) {
                user.setActive(true);
                user.setUpdateTime(date);
                userMapper.activeUser(user);
            }
            RelUserOrganization relUserOrganization = relUserOrganizationMapper.getRel(user.getId(), organization.getId());
            if (relUserOrganization == null) {
                //加入admin组织
                joinAdminOrganization(organization, user.getId());
            }

        } else {
            user = new User();
            user.setEmail(ucinAccess.getEmail());
            user.setUsername(ucinAccess.getPptName());
            user.setPassword(Constants.LDAP_USER_PASSWORD);
            user.setAdmin(true);
            user.setActive(true);
            user.setCreateTime(date);
            user.setCreateBy(0L);
            user.setName(ucinAccess.getName());
            //新增用户
            int insert = userMapper.insert(user);
            Long userId = user.getId();
            if (insert > 0) {
                String OrgName = user.getUsername() + "'s Organization";
                Organization organizationOwn = new Organization(OrgName, null, userId);
                int i = organizationMapper.insert(organizationOwn);
                if (i > 0) {
                    RelUserOrganization relUserOrganization = new RelUserOrganization(organizationOwn.getId(), user.getId(), UserOrgRoleEnum.OWNER.getRole());
                    relUserOrganizationMapper.insert(relUserOrganization);
                }

            } else {
                throw new ServerException("unknown fail");
            }
            //加入admin组织
            joinAdminOrganization(organization, userId);
        }
    }

    /**
     * 加入admin的组织
     *
     * @param organization
     * @param userId
     */
    private void joinAdminOrganization(Organization organization, Long userId) {
        //加入admin组织
        RelUserOrganization rel = new RelUserOrganization(organization.getId(), userId, UserOrgRoleEnum.MEMBER.getRole());
        int insert = relUserOrganizationMapper.insert(rel);
        if (insert > 0) {
            //修改成员人数
            organization.setMemberNum(organization.getMemberNum() + 1);
            organizationMapper.updateMemberNum(organization);
        } else {
            throw new ServerException("unknown fail");
        }
    }

    /**
     * 从admin的组织移除
     *
     * @param relationId
     * @param user
     */
    private void removeAdminOrganization(Long relationId, User user) {
        organizationService.deleteOrgMember(relationId, user);
    }

    /**
     * 用户禁用
     *
     * @param ucinAccess
     * @return
     */
    public void userRemove(UcinAccess ucinAccess) throws ServerException {
        Date date = new Date();
        User user = userMapper.selectByUsername(ucinAccess.getPptName());
        //获取超级管理员账号
        User userAdmin = userMapper.selectByUsername(adminUsername);
        if (userAdmin == null) {
            log.error("ucin-access MQ Hanlder remove error :admin user is null");
            return;
        }
        Organization organization = organizationMapper.getByUserId(userAdmin.getId());
        if (organization == null) {
            log.error("ucin-access MQ Hanlder remove error :admin's Organization is null");
            return;
        }
        //用户账号是否已经存在
        if (user != null) {
            user.setActive(false);
            user.setUpdateTime(date);
            userMapper.activeUser(user);
            RelUserOrganization relUserOrganization = relUserOrganizationMapper.getRel(user.getId(), organization.getId());
            if(relUserOrganization != null){
                removeAdminOrganization(relUserOrganization.getId(),userAdmin);
            }
        }
    }
}

