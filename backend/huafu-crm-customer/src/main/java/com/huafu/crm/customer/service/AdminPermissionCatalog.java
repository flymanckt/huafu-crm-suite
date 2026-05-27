package com.huafu.crm.customer.service;

import com.huafu.crm.customer.dto.MenuNodeVO;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AdminPermissionCatalog {
    private final List<MenuNodeVO> menuTree = List.of(
        node(1000L, "工作台", "dashboard:view", "/dashboard"),
        node(2000L, "客户管理", "customer", null,
            node(2100L, "客户列表", "customer:list", "/customer"),
            node(2200L, "公海客户", "customer:public-pool", "/customer/public-pool")
        ),
        node(3000L, "商机管理", "opportunity", null,
            node(3100L, "线索商情", "opportunity:lead", "/opportunity/lead"),
            node(3200L, "商机列表", "opportunity:list", "/opportunity"),
            node(3300L, "丢单记录", "opportunity:lost", "/opportunity/lost")
        ),
        node(4000L, "展会管理", "exhibition", null,
            node(4100L, "展会活动", "business:exhibition-activity", "/business/exhibition-activity"),
            node(4200L, "参展物资清单", "business:exhibition-material", "/business/exhibition-material"),
            node(4300L, "展会样品库", "business:exhibition-sample", "/business/exhibition-sample"),
            node(4400L, "展会洽谈记录", "business:exhibition-talk", "/business/exhibition-talk")
        ),
        node(5000L, "产品与样品", "sample", null,
            node(5100L, "产品档案", "business:product-master", "/business/product-master"),
            node(5200L, "样品库存", "business:sample-inventory", "/business/sample-inventory"),
            node(5300L, "样品出库单", "business:sample-outbound", "/business/sample-outbound")
        ),
        node(6000L, "色卡派送", "color-card", null,
            node(6100L, "色卡派送计划", "business:color-card-plan", "/business/color-card-plan"),
            node(6200L, "色卡派送总表", "business:color-card-ledger", "/business/color-card-ledger")
        ),
        node(7000L, "订单管理", "order", null,
            node(7100L, "销售订单", "business:sales-order", "/business/sales-order"),
            node(7200L, "发货单", "business:shipment", "/business/shipment")
        ),
        node(8000L, "目标管理", "target", null,
            node(8100L, "目标与达成", "target:list", "/target")
        ),
        node(9000L, "报价管理", "quote", null,
            node(9100L, "报价单管理", "quote:list", "/quote")
        ),
        node(10000L, "系统管理", "admin", null,
            node(10100L, "用户管理", "admin:user", "/admin/user"),
            node(10200L, "角色管理", "admin:role", "/admin/role"),
            node(10300L, "部门管理", "admin:dept", "/admin/dept"),
            node(10400L, "字典管理", "system:dict", "/system/dict"),
            node(10500L, "外围系统配置", "system:config", "/system/config"),
            node(10600L, "集成平台", "system:integration", "/system/integration"),
            node(10700L, "企微消息日志", "wecom:message", "/wecom/message")
        ),
        node(11000L, "勤力度", "performance", null,
            node(11100L, "拜访记录", "performance:visit", "/performance/visit"),
            node(11200L, "日报列表", "performance:daily-report", "/performance/daily-report"),
            node(11300L, "勤力度评分", "performance:list", "/performance")
        ),
        node(12000L, "报表与日报", "report", null,
            node(12100L, "自定义报表", "business:custom-report", "/business/custom-report"),
            node(12200L, "工作日报", "business:work-daily-report", "/business/work-daily-report")
        ),
        node(13000L, "AI解析", "ai:view", "/ai")
    );

    private final Map<Long, MenuNodeVO> byId = new LinkedHashMap<>();

    public AdminPermissionCatalog() {
        flatten(menuTree).forEach(item -> byId.put(item.id(), item));
    }

    public List<MenuNodeVO> menuTree() {
        return menuTree;
    }

    public List<Long> allMenuIds() {
        return new ArrayList<>(byId.keySet());
    }

    public List<String> permissions(List<Long> menuIds) {
        if (menuIds == null || menuIds.isEmpty()) return List.of();
        return menuIds.stream()
            .map(byId::get)
            .filter(menu -> menu != null && menu.permission() != null && !menu.permission().isBlank())
            .map(MenuNodeVO::permission)
            .distinct()
            .toList();
    }

    private static MenuNodeVO node(Long id, String name, String permission, String path, MenuNodeVO... children) {
        return new MenuNodeVO(id, name, permission, path, children == null ? List.of() : List.of(children));
    }

    private static List<MenuNodeVO> flatten(List<MenuNodeVO> nodes) {
        List<MenuNodeVO> result = new ArrayList<>();
        for (MenuNodeVO node : nodes) {
            result.add(node);
            result.addAll(flatten(node.children()));
        }
        return result;
    }
}
