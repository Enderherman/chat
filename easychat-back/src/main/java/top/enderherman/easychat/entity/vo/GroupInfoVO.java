package top.enderherman.easychat.entity.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import top.enderherman.easychat.entity.po.GroupInfo;
import top.enderherman.easychat.entity.po.UserContact;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GroupInfoVO {

    private GroupInfo groupInfo;
    private List<UserContact> userContactList;
}
