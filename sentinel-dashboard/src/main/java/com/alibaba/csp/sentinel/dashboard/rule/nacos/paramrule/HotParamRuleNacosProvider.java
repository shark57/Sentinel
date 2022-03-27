package com.alibaba.csp.sentinel.dashboard.rule.nacos.paramrule;

import com.alibaba.csp.sentinel.dashboard.config.NacosConfigUtil;
import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 原因是：改造dashboard，提交到nacos配置中心的数据是ParamFlowRuleEntity类型，微服务拉取配置要解析的是ParamFlowRule类型，
 * 会导致规则解析丢失数据，造成热点规则不生效。
 * 我用了两种方式解决了这个问题：
 * 第一种：自定义一个解析热点规则配置的解析器FlowParamJsonConverter，继承JsonConverter，重写convert方法。
 * 然后利用后置处理器替换beanName为"param-flow-rules-sentinel-nacos-datasource"的converter属性，注入FlowParamJsonConverter。
 * 第二种：改造Sentinel Dashboard控制台，发布配置时将ParamFlowRuleEntity转成ParamFlowRule类型，
 * 再发布到Nacos配置中心。从配置中心拉取配置后将ParamFlowRule转成ParamFlowRuleEntity。
 * 热点参数
 *
 * @author wqi
 * @date 2022/3/22
 */
@Component("hotParamRuleNacosProvider")
public class HotParamRuleNacosProvider implements DynamicRuleProvider<List<ParamFlowRuleEntity>> {

    @Autowired
    private ConfigService configService;
    @Autowired
    private Converter<String, List<ParamFlowRuleEntity>> converter;

//
//    @Override
//    public List<ParamFlowRuleEntity> getRules(String appName) throws Exception {
//        String rules = configService.getConfig(appName + NacosConfigUtil.PARAM_FLOW_DATA_ID_POSTFIX,
//                NacosConfigUtil.GROUP_ID, 3000);
//        if (StringUtil.isEmpty(rules)) {
//            return new ArrayList<>();
//        }
//        return converter.convert(rules);
//    }

    @Override
    public List<ParamFlowRuleEntity> getRules(String appName) throws Exception {
        String rules = configService.getConfig(appName + NacosConfigUtil.PARAM_FLOW_DATA_ID_POSTFIX,
                NacosConfigUtil.GROUP_ID, 3000);
        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        List<ParamFlowRuleEntity> convert = converter.convert(rules);
        for (ParamFlowRuleEntity paramFlowRuleEntity : convert) {
            paramFlowRuleEntity.setApp(appName);
        }
        return convert;
    }

}
