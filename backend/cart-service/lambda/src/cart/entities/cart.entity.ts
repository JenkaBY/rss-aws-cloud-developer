import { Column, CreateDateColumn, Entity, OneToMany, PrimaryGeneratedColumn, UpdateDateColumn } from 'typeorm';
import { CartItem } from './cart-item.entity';
import { CartStatus } from '../models';

@Entity('carts')
export class Cart {

  @PrimaryGeneratedColumn('uuid')
  id?: string;

  @Column()
  user_id: string;

  @CreateDateColumn({ type: 'timestamptz', default: () => 'CURRENT_TIMESTAMP' })
  created_at?: Date;

  @UpdateDateColumn({ type: 'timestamptz', default: () => 'CURRENT_TIMESTAMP' })
  updated_at?: Date;

  @Column({ type: 'varchar', default: () => 'OPEN' })
  status: CartStatus;

  @OneToMany(() => CartItem,
    (cartItem) => cartItem.cart,
    {
      cascade: ['insert', 'update', 'remove'],
      orphanedRowAction: 'delete',
      eager: true
    })
  items: CartItem[];
}