import { Controller, Get, HttpStatus, Post, Request, UseGuards } from '@nestjs/common';
import { AuthService, BasicAuthGuard } from './auth';
import { LocalAuthGuard } from './auth/guards/local-auth.guard';

@Controller()
export class AppController {

  constructor(private authService: AuthService) {
  }

  @Get(['', 'ping'])
  healthCheck(): any {
    return {
      statusCode: HttpStatus.OK,
      message: 'OK',
    };
  }

  @UseGuards(LocalAuthGuard)
  @Post('api/auth/login')
  async login(@Request() req) {
    console.log('req header', req.headers);
    const token = this.authService.login(req.user, 'basic');

    return {
      statusCode: HttpStatus.OK,
      message: 'OK',
      data: {
        ...token,
      },
    };
  }

  @UseGuards(BasicAuthGuard)
  @Get('api/profile')
  async getProfile(@Request() req) {
    console.log('req headers', req.headers);
    return {
      statusCode: HttpStatus.OK,
      message: 'OK',
      data: {
        user: req.user,
      },
    };
  }
}
