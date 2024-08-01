import { Controller, Get, HttpStatus, Req } from '@nestjs/common';
import { AppRequest } from '../shared';
import { OrderService } from '../order';

@Controller('api/order')
export class OrderController {
  constructor(
    private orderService: OrderService
  ) {
  }

  // @UseGuards(JwtAuthGuard)
  // @UseGuards(BasicAuthGuard)
  @Get()
  async getOrders(@Req() req: AppRequest) {
    console.log('request', req.body, req.headers);
    const orders = await this.orderService.getOrders();

    return {
      statusCode: HttpStatus.OK,
      message: 'OK',
      orders
    };
  }
}
