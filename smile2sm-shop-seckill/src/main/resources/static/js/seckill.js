/** 秒杀js模块*/
var seckill = {
    VAL: {
        seckill_id: 0,
        intervX: 0
    },
    /** 秒杀相关url*/
    URL: {
    	/** 获取当前时间*/
        now: function () {
            return '/seckill/time/now';
        },
        /** 是否暴露秒杀地址*/
        exposer: function (seckill_id) {
            return '/seckill/exposer/' + seckill_id;
        },
        /** 执行秒杀地址*/
        execution: function (seckill_id, phone,md5) {
            return '/seckill/executeSeckill/' + seckill_id + '/' + phone+'/'+md5;
        },
        /** 查询秒杀结果*/
        isGrab: function (seckill_id, phone) {
            return '/seckill/isGrab/' + seckill_id + '/' + phone;
        }
    },
    
    /** 方法区*/
    /** 秒杀处理*/
    handleSeckill: function (seckill_id, node) {
        /** 添加显示按钮*/
        node.hide().html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');
       /** 获取秒杀地址*/
        $.post(seckill.URL.exposer(seckill_id),
            {},
            function (result) {
                if (result && result.code==0) {
                    var exposer = result.data;
                    if (exposer.exposed) {
                        //开启秒杀
                        seckill.VAL.seckill_id = seckill_id;
                        var phone = $.cookie('seckillPhone');
                        var killUrl = seckill.URL.execution(seckill_id, phone,exposer.md5);
                        console.log("killUrl:" + killUrl);
                        
                        /** 绑定一次点击事件,执行秒杀*/
                        $('#killBtn').one('click', function () {
                        	 /** 1、禁用按钮，避免重复提交*/
                            $(this).addClass('disabled');
                            /** 2、post发送秒杀请求，返回秒杀结果*/
                            $.post(killUrl, {}, function (result) {
                                if (result) {
                                    var code = result.code;
                                    var msg = result.msg;
                                    //3:显示秒杀结果
                                    node.html('<span class="label label-success">' + msg + '</span>');

                                    if (code === 50003) {
                                       seckill.VAL.intervX = window.setInterval(seckill.isGrab, 1000);
                                    }

                                }
                            });
                        });
                        node.show();
                    } else {
                        //重新计算计时逻辑
                    	alert(exposer.seckill_now_time);
                        seckill.countdown(seckill_id, 
                        		exposer.seckill_now_time, 
                        		exposer.seckill_start_time,
                        		exposer.seckill_end_time);
                    }
                } else {
                    console.log('result:' + result);
                }

            });
    },
    //验证手机号
    validatePhone: function (phone) {
        if (phone && phone.length == 11 && !isNaN(phone)) {
            return true;
        } else {
            return false;
        }
    },
    countdown: function (seckillId, nowTime, startTime, endTime) {
        var seckillBox = $('#seckill-box');
        //时间判断
        if (nowTime > endTime) {
            //秒杀结束
            seckillBox.html('秒杀结束!');
        } else if (nowTime < startTime) {
            //秒杀未开始,计时事件绑定
            var killTime = new Date(startTime + 1000);
            
            seckillBox.countdown(killTime, function (event) {
                //时间格式
                var format = event.strftime('秒杀倒计时: %D天 %H时 %M分 %S秒');
                seckillBox.html(format);
                /*时间完成后回调事件*/
            }).on('finish.countdown', function () {
                seckill.handleSeckill(seckillId, seckillBox);
            });
        } else {
            //秒杀开始
        	seckill.handleSeckill(seckillId, seckillBox);
        }
    },
    //详情页秒杀逻辑
    detail: {
        //详情页初始化
        init: function (params) {
            //手机验证和登录 , 计时交互
            //规划我们的交互流程
            //在cookie中查找手机号
            var killPhone = $.cookie('seckillPhone');
            //验证手机号
            if (!seckill.validatePhone(killPhone)) {
                //绑定phone
                //控制输出
                var killPhoneModal = $('#killPhoneModal');
                //显示弹出层
                killPhoneModal.modal({
                    show: true,//显示弹出层
                    backdrop: 'static',//禁止位置关闭
                    keyboard: false//关闭键盘事件
                });
                $('#killPhoneBtn').click(function () {
                    var inputPhone = $('#killPhoneKey').val();
                    console.log('inputPhone=' + inputPhone);//TODO
                    if (seckill.validatePhone(inputPhone)) {
                        //电话写入cookie
                        $.cookie('seckillPhone', inputPhone, {expires: 7, path: '/seckill'});
                        //刷新页面
                        window.location.reload();
                    } else {
                        $('#killPhoneMessage').hide().html('<label class="label label-danger">手机号错误!</label>').show(300);
                    }
                });
            }
            //已经登录
            //计时交互
            var startTime = params['startTime'];
            var endTime = params['endTime'];
            var seckillId = params['seckillId'];
           
            $.get(seckill.URL.now(), {}, function (result) {
                if (result && result['code']==0) {
                    var nowTime = result['data'];
                    //时间判断,计时交互
                    seckill.countdown(seckillId,Date.parse(nowTime), startTime, endTime);
                } else {
                    console.log('result:' + result);
                }
            });


        }
    },
    isGrab: function () {
        var node = $('#seckill-box');
        var currentPhone = $.cookie('killPhone');
        $.post(seckill.URL.isGrab(seckill.VAL.seckill_id, currentPhone),
            {},
            function (result) {
                if (result.code == 50003) {
                    console.log(">>>>秒杀排队中...");
                    node.html('<span class="label label-success">' + "排队中..." + '</span>');
                } else {
                    if (seckill.VAL.intervX != 0) {
                        window.clearInterval(seckill.VAL.intervX);
                    }

                    if (result.code == 0) {
                        console.log(">>>>秒杀成功");
                        node.html('<span class="label label-success">' + "秒杀成功" + '</span>');
                    } else if (result == 2) {
                        console.log(">>>>没抢到！");
                        node.html('<span class="label label-success">' + "没抢到" + '</span>');
                    }
                }

            });
    }

}