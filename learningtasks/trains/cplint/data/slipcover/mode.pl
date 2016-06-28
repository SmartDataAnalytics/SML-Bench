output(eastbound/0).    %ariety is 1 parameter less

input_cw(short/1).
input_cw(long/1).
input_cw(closed/1).
input_cw(open_car/1).
input_cw(double/1).
input_cw(jagged/1).
input_cw(shape/2).
input_cw(load/3).
input_cw(wheels/2).
input_cw(has_car/1).    %ariety is 1 parameter less

modeh(1,eastbound).
modeb(*,has_car(-car)).
modeb(1,short(+car)).
modeb(1,closed(+car)).
modeb(1,long(+car)).
modeb(1,open_car(+car)).
modeb(1,double(+car)).
modeb(1,jagged(+car)).
modeb(1,shape(+car,#shape)).
modeb(1,load(+car,#shape,#int)).
modeb(1,wheels(+car,#int)).

determination(eastbound/0,short/1).
determination(eastbound/0,closed/1).
determination(eastbound/0,long/1).
determination(eastbound/0,open_car/1).
determination(eastbound/0,double/1).
determination(eastbound/0,jagged/1).
determination(eastbound/0,shape/2).
determination(eastbound/0,wheels/2).
determination(eastbound/0,has_car/1).
determination(eastbound/0,load/3).
