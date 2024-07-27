import { Injectable } from '@nestjs/common';

import { v4 } from 'uuid';

import { UserDto } from '../models';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { User } from '../entities';

@Injectable()
export class UsersService {

  constructor(@InjectRepository(User) private readonly userRepo: Repository<User>) {
  }

  async findOne(name: string): Promise<UserDto> {
    return await this.userRepo.findOneBy({
      name: name,
    }).then(user => user as UserDto);
  }

  async createOne({ name, password }: Partial<UserDto>): Promise<UserDto> {
    console.log("create User name ", name, "password", password);
    const id = v4();
    const newUser = { id, name, password } as User;
    console.log("newUser", newUser);
    return await this.userRepo.save(newUser).then(user => user as UserDto);
  }

}
