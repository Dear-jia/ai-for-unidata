<template>
  <div class="page-container">
    <el-card shadow="never">
      <div class="section-title">{{ title }}</div>
      <div class="legal-body" v-html="content"></div>
      <el-alert type="info" :closable="false" show-icon style="margin-top: 20px"
        title="提示：以上文本为基础模板，正式上线前建议由专业法律人士结合实际情况审核完善" />
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'

const route = useRoute()

const docs = {
  disclaimer: {
    title: '免责声明',
    content: `
      <h3>一、数据说明</h3>
      <p>本平台收录的院校名单、主管部门、国家线等数据，来源于教育部及各省级教育考试机构公开信息；院校信息页与官方复试线原文链接指向研招网及各院校官方渠道。</p>
      <p>平台数据可能存在更新滞后或录入误差，所有分数线、招生信息均<strong>以教育部、各招生单位官方最新公布为准</strong>。</p>
      <h3>二、参考用途</h3>
      <p>本平台提供的数据与分析内容仅供用户备考择校参考，不构成任何录取承诺或官方结论。因参考本平台数据作出的报考、调剂等决策，由用户自行承担相应后果。</p>
      <h3>三、第三方内容</h3>
      <p>平台中的官方原文链接指向第三方网站（如研招网、各高校官网），第三方网页内容的知识产权归其权利人所有，本平台不复制、不存储其图表内容。</p>
      <h3>四、联系方式</h3>
      <p>如发现数据错误或内容侵权问题，请联系平台管理员进行更正或删除。</p>
    `
  },
  privacy: {
    title: '隐私政策',
    content: `
      <h3>一、我们收集的信息</h3>
      <p>注册时：用户名、密码（经加密存储）、昵称；选填信息：邮箱、手机号；使用过程中：基础访问日志（IP、时间、页面）。</p>
      <h3>二、信息的使用</h3>
      <p>以上信息仅用于账号登录与身份验证、提供平台服务、保障账号与系统安全，不会用于其他用途。</p>
      <h3>三、信息的共享</h3>
      <p>我们不会出售、出租或以其他方式向第三方提供您的个人信息，法律法规另有规定或经您明确同意的除外。</p>
      <h3>四、信息的存储与安全</h3>
      <p>您的密码经 BCrypt 单向加密存储，平台采用访问控制与安全配置保护数据。本地登录凭证仅保存在您自己的设备中。</p>
      <h3>五、您的权利</h3>
      <p>您可以随时查看、修改您的个人资料；如需注销账号或删除个人信息，请联系平台管理员处理。</p>
      <h3>六、未成年人保护</h3>
      <p>本平台主要面向成年备考用户。如您是未成年人，请在监护人指导下使用。</p>
    `
  },
  terms: {
    title: '用户协议',
    content: `
      <h3>一、服务说明</h3>
      <p>本平台提供考研院校信息、历年分数线、考研资讯等数据服务。使用本平台即表示您同意本协议全部条款。</p>
      <h3>二、账号规范</h3>
      <p>注册信息应真实有效；妥善保管账号密码，因账号保管不善造成的损失由用户自行承担；不得转让、出借账号。</p>
      <h3>三、用户行为规范</h3>
      <p>不得利用本平台从事任何违法活动；不得批量抓取、复制平台数据用于商业竞争；不得发布违法、侵权或不良信息。</p>
      <h3>四、知识产权</h3>
      <p>平台自有页面设计、代码与原创内容的著作权归本平台所有；收录的公开数据版权归原始权利人所有，本平台仅作信息聚合展示并标注来源。</p>
      <h3>五、服务变更与终止</h3>
      <p>平台有权根据运营需要调整或终止部分服务；对违反本协议的用户，平台有权限制或终止其账号使用。</p>
      <h3>六、法律适用</h3>
      <p>本协议适用中华人民共和国法律。因本协议产生的争议，双方应友好协商解决，协商不成的提交平台运营所在地有管辖权的人民法院处理。</p>
    `
  }
}

const title = computed(() => docs[route.params.type]?.title || '免责声明')
const content = computed(() => docs[route.params.type]?.content || docs.disclaimer.content)
</script>

<style scoped>
.legal-body {
  line-height: 1.9;
  color: #303133;
  font-size: 14px;
}

.legal-body h3 {
  font-size: 15px;
  margin: 18px 0 6px;
}

.legal-body p {
  margin: 6px 0;
  text-indent: 2em;
}
</style>
