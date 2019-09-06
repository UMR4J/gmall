package com.atguigu.gmall.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmall.bean.OrderDetail;
import com.atguigu.gmall.bean.OrderInfo;
import com.atguigu.gmall.bean.enums.ProcessStatus;
import com.atguigu.gmall.config.ActiveMQUtil;
import com.atguigu.gmall.config.RedisUtil;
import com.atguigu.gmall.order.mapper.OrderDetailMapper;
import com.atguigu.gmall.order.mapper.OrderInfoMapper;
import com.atguigu.gmall.service.OrderService;
import com.atguigu.gmall.service.PaymentService;
import com.atguigu.gmall.util.HttpClientUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.Connection;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import java.util.*;

/**
 * @author zdy
 * @create 2019-08-30 21:10
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private ActiveMQUtil activeMQUtil;

    @Reference
    private PaymentService paymentService;

    @Transactional
    @Override
    public String saveOrder(OrderInfo orderInfo) {

        //订单的创建时间
        orderInfo.setCreateTime(new Date());
        //订单的失效时间
        Calendar calendar=Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(calendar.getTime());
        // 生成第三方支付编号
        String outTradeNo="ATGUIGU"+System.currentTimeMillis()+""+new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfoMapper.insertSelective(orderInfo);

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);
        }

        // 为了跳转到支付页面使用。支付会根据订单id进行支付。
        String orderId = orderInfo.getId();
        return orderId;

    }

    @Override
    public String getTradeNo(String userId) {

        Jedis jedis=null;
        try {

            jedis= redisUtil.getJedis();
            String tradeNoKey="user:"+userId+":tradeCode";
            String tradeCode = UUID.randomUUID().toString();
            jedis.setex(tradeNoKey,10*60,tradeCode);
            return tradeCode;

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
        return null;
    }

    @Override
    public boolean checkTradeCode(String userId, String tradeCodeNo) {

        Jedis jedis=null;
        try {

            jedis= redisUtil.getJedis();
            String tradeNoKey="user:"+userId+":tradeCode";
            String codeNo = jedis.get(tradeNoKey);
            if(tradeCodeNo!=null){
                return tradeCodeNo.equals(codeNo);
            }else {
                return false;
            }

        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }
        return false;
    }

    @Override
    public void delTradeCode(String userId) {

        Jedis jedis=null;
        try {

            jedis= redisUtil.getJedis();
            String tradeNoKey="user:"+userId+":tradeCode";
            jedis.del(tradeNoKey);
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if(jedis!=null){
                jedis.close();
            }
        }

    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {

        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);


        return "1".equals(result);
    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {
        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);

        OrderDetail orderDetail=new OrderDetail();
        orderDetail.setOrderId(orderId);
        List<OrderDetail> orderDetailList = orderDetailMapper.select(orderDetail);

        orderInfo.setOrderDetailList(orderDetailList);

        return orderInfo;

    }

    @Override
    public void updateOrderStatus(String orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo=new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);

    }

    @Override
    public void sendOrderStatus(String orderId) {

        String orderJson = initWareOrder(orderId);

        try {
            Connection connection = activeMQUtil.getConnection();
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");
            MessageProducer producer = session.createProducer(order_result_queue);

            ActiveMQTextMessage activeMQTextMessage=new ActiveMQTextMessage();
            activeMQTextMessage.setText(orderJson);

            producer.send(activeMQTextMessage);

            session.commit();

            producer.close();
            session.close();
            connection.close();

        }catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public List<OrderInfo> getExpiredOrderList() {

        Example example=new Example(OrderInfo.class);
        example.createCriteria()
                .andLessThan("expireTime", new Date())
                .andEqualTo("processStatus",ProcessStatus.UNPAID);

        List<OrderInfo> orderInfoList = orderInfoMapper.selectByExample(example);


        return orderInfoList;
    }

    @Async
    @Override
    public void execExpiredOrder(OrderInfo orderInfo) {
        updateOrderStatus(orderInfo.getId(), ProcessStatus.CLOSED);
        paymentService.closePayment(orderInfo.getId());
    }

    private String initWareOrder(String orderId) {

        OrderInfo orderInfo = getOrderInfo(orderId);
        Map map = initWareOrder(orderInfo);
        return JSON.toJSONString(map);

    }
    @Override
    public Map initWareOrder(OrderInfo orderInfo) {
        Map<String,Object> map = new HashMap<>();
        map.put("orderId",orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel",orderInfo.getConsigneeTel());
        map.put("orderComment",orderInfo.getOrderComment());
        map.put("orderBody",orderInfo.getTradeBody());
        map.put("deliveryAddress",orderInfo.getDeliveryAddress());
        map.put("paymentWay","2");
        map.put("wareId",orderInfo.getWareId());

        List detailList = new ArrayList();
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();

        for (OrderDetail orderDetail : orderDetailList) {
            Map detailMap = new HashMap();
            detailMap.put("skuId",orderDetail.getSkuId());
            detailMap.put("skuName",orderDetail.getSkuName());
            detailMap.put("skuNum",orderDetail.getSkuNum());
            detailList.add(detailMap);

        }
        map.put("details",detailList);

        return map;
    }

    /**
     *
     * @param orderId
     * @param wareSkuMap:仓库编号与商品的对照关系
     * 例如
     * [{"wareId":"1","skuIds":["2","10"]},{"wareId":"2","skuIds":["3"]}]
     * 表示：sku为2号，10号的商品在1号仓库
     *  sku为3号的商品在2号仓库
     * @return
     */
    @Override
    public List<OrderInfo> splitOrder(String orderId, String wareSkuMap) {
        //定义子订单集合
        List<OrderInfo> subOrderInfoList = new ArrayList<>();
        //获取原始订单
        OrderInfo orderInfoOrigin = getOrderInfo(orderId);
        //反序列化wareSkuMap
        List<Map> maps = JSON.parseArray(wareSkuMap, Map.class);

        for (Map map : maps) {
            String wareId = (String) map.get("wareId");
            List<String> skuIds = (List<String>) map.get("skuIds");
            //生成新的subOrderInfo
            OrderInfo subOrderInfo =new OrderInfo();
            //基本信息从原始订单复制
            BeanUtils.copyProperties(orderInfoOrigin, subOrderInfo );
            subOrderInfo.setId(null);//生成新的id
            subOrderInfo.setParentOrderId(orderInfoOrigin.getId());//设置父订单号
            subOrderInfo.setWareId(wareId);//设置仓库id

            //处理订单明细
            List<OrderDetail> orderDetailList = orderInfoOrigin.getOrderDetailList();
            //新创建子订单明细
            List<OrderDetail> subOrderDetailList = new ArrayList<>();
            for (OrderDetail orderDetail : orderDetailList) {
                for (String skuId : skuIds) {
                    if(skuId.equals(orderDetail.getSkuId())){
                        orderDetail.setId(null);
                        subOrderDetailList.add(orderDetail);
                    }
                }

            }

            subOrderInfo.setOrderDetailList(subOrderDetailList);
            subOrderInfo.sumTotalAmount();
            //保存到数据库
            saveOrder(subOrderInfo);
            subOrderInfoList.add(subOrderInfo);

        }

        updateOrderStatus(orderId,ProcessStatus.SPLIT);


        //返回子订单集合
        return subOrderInfoList;
    }
}
