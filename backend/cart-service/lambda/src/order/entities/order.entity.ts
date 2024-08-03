import { Column, Entity, JoinColumn, ManyToOne, PrimaryGeneratedColumn } from 'typeorm';
import { Cart } from '../../cart';

@Entity('orders')
export class Order {
  @PrimaryGeneratedColumn('uuid')
  id?: string;

  @Column({ name: 'user_id' })
  userId: string;

  @ManyToOne(
    () => Cart)
  @JoinColumn({ name: 'cart_id' })
  cart: Cart;

  @Column({ type: 'jsonb' })
  delivery: any;

  @Column({ type: 'varchar', length: 100 })
  status: string;

  @Column({ type: 'varchar', length: 1000 })
  comments: string;

  @Column({ type: 'decimal' })
  total: number;
}