package cn.itcast.hotel.web;

import cn.itcast.hotel.constants.MessageQueueConstants;
import cn.itcast.hotel.pojo.Hotel;
import cn.itcast.hotel.pojo.PageResult;
import cn.itcast.hotel.service.IHotelService;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.security.InvalidParameterException;

@RestController
@RequestMapping("hotel")
public class HotelController {

    @Autowired
    private IHotelService hotelService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("/{id}")
    public Hotel queryById(@PathVariable("id") Long id) {
        return hotelService.getById(id);
    }

    @GetMapping("/list")
    public PageResult hotelList(
            @RequestParam(value = "page", defaultValue = "1") Integer page,
            @RequestParam(value = "size", defaultValue = "1") Integer size
    ) {
        Page<Hotel> result = hotelService.page(new Page<>(page, size));

        return new PageResult(result.getTotal(), result.getRecords());
    }

    /**
     * 新增
     */
    @PostMapping
    public void saveHotel(@RequestBody Hotel hotel) {
        hotelService.save(hotel);

        // 发送消息                                 交换机名称                           路由key                            消息内容
        rabbitTemplate.convertAndSend(MessageQueueConstants.HOTEL_EXCHANGE, MessageQueueConstants.HOTEL_INSERT_KEY, hotel.getId());
    }

    /**
     * 更新
     */
    @PutMapping()
    public void updateById(@RequestBody Hotel hotel) {
        if (hotel.getId() == null) {
            throw new InvalidParameterException("id不能为空");
        }
        hotelService.updateById(hotel);

        // 发送消息                                 交换机名称                           路由key                            消息内容
        rabbitTemplate.convertAndSend(MessageQueueConstants.HOTEL_EXCHANGE, MessageQueueConstants.HOTEL_INSERT_KEY, hotel.getId());
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable("id") Long id) {
        hotelService.removeById(id);

        // 发送消息                                 交换机名称                           路由key                      消息内容
        rabbitTemplate.convertAndSend(MessageQueueConstants.HOTEL_EXCHANGE, MessageQueueConstants.HOTEL_DELETE_KEY, id);
    }
}
