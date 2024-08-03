import { Injectable } from '@nestjs/common';
import { UserDto, UsersService } from '../users';

@Injectable()
export class AuthService {
  constructor(private readonly usersService: UsersService) {
  }

  async verifyUser(name: string, pass: string): Promise<UserDto> {
    console.log("verify user ", name)
    const user = await this.usersService.findOne(name);
    console.log('found user ', user);
    if (user && user.password === pass) {
      return user;
    }
  }

  async validateUser(name: string, pass: string): Promise<UserDto> {
    const user = await this.usersService.findOne(name);
    if (user) {
      if (user.password !== pass) {
        return null;
      }
      return user;
    }
    return await this.usersService.createOne({ name, password: pass });
  }

  login(user: UserDto, type: 'basic' | 'default' | 'bearer') {
    const LOGIN_MAP = {
      basic: this.loginBasic,
      default: this.loginBasic,
    };
    const login = LOGIN_MAP[type];

    return login ? login(user) : LOGIN_MAP.default(user);
  }

  loginBasic(user: UserDto) {
    function encodeUserToken(user) {
      const { name, password } = user;
      console.log("enc ", name, password)
      const buf = Buffer.from([name, password].join(':'), 'utf8');

      return buf.toString('base64');
    }

    return {
      token_type: 'Basic',
      access_token: encodeUserToken(user),
    };
  }


}
