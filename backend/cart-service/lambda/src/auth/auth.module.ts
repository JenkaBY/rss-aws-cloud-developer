import { Module } from '@nestjs/common';
import { PassportModule } from '@nestjs/passport';

import { AuthService } from './auth.service';
import { BasicStrategy, LocalStrategy } from './strategies';
import { UsersModule } from '../users/users.module';


@Module({
  imports: [UsersModule, PassportModule.register({ defaultStrategy: 'basic' })],
  providers: [AuthService, BasicStrategy, LocalStrategy],
  exports: [AuthService],
})
export class AuthModule {
}
