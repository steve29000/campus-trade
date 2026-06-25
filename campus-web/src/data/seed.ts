// 种子数据：让闭环现在就能跑。时间戳相对加载时刻生成，保证"x分钟前"始终新鲜。
import type {
  User,
  Category,
  Product,
  Favorite,
  ChatSession,
  Message,
  Verification,
  Report,
} from '../domain/types';

export interface SeedData {
  users: User[];
  categories: Category[];
  products: Product[];
  favorites: Favorite[];
  sessions: ChatSession[];
  messages: Message[];
  verifications: Verification[];
  reports: Report[];
}

const MIN = 60 * 1000;
const HOUR = 60 * MIN;
const DAY = 24 * HOUR;

// 当前演示用户固定为 u1（已认证 · 南校区）。
export const DEMO_USER_ID = 'u1';
// 一个未认证账号，用于演示"认证前置"守卫。
export const DEMO_UNVERIFIED_USER_ID = 'u5';

function img(count: number) {
  // 占位图：url 为空时由 <ProductThumb> 渲染 渐变+emoji，离线安全。
  return Array.from({ length: count }, (_, i) => ({ url: '', sortOrder: i }));
}

export function buildSeed(now: number): SeedData {
  const users: User[] = [
    {
      id: 'u1',
      nickname: '小南（我）',
      avatar: '🦊',
      email: 'nan@stu.edu.cn',
      school: '示范大学',
      campus: 'SOUTH',
      studentNo: '2021SOUTH001',
      verifyStatus: 'VERIFIED',
      role: 'STUDENT',
      status: 'ACTIVE',
      rating: 4.9,
      createdAt: now - 60 * DAY,
    },
    {
      id: 'u2',
      nickname: '北区学长',
      avatar: '🐻',
      school: '示范大学',
      campus: 'NORTH',
      studentNo: '2020NORTH018',
      verifyStatus: 'VERIFIED',
      role: 'STUDENT',
      status: 'ACTIVE',
      rating: 4.7,
      createdAt: now - 120 * DAY,
    },
    {
      id: 'u3',
      nickname: '考研的喵',
      avatar: '🐱',
      school: '示范大学',
      campus: 'SOUTH',
      studentNo: '2022SOUTH233',
      verifyStatus: 'VERIFIED',
      role: 'STUDENT',
      status: 'ACTIVE',
      rating: 4.8,
      createdAt: now - 90 * DAY,
    },
    {
      id: 'u4',
      nickname: '毕业甩卖君',
      avatar: '🐧',
      school: '示范大学',
      campus: 'NORTH',
      studentNo: '2019NORTH077',
      verifyStatus: 'VERIFIED',
      role: 'STUDENT',
      status: 'ACTIVE',
      rating: 4.6,
      createdAt: now - 200 * DAY,
    },
    {
      id: 'u5',
      nickname: '新生小白',
      avatar: '🐤',
      school: '示范大学',
      campus: 'NORTH',
      studentNo: '2024NORTH500',
      verifyStatus: 'PENDING',
      role: 'STUDENT',
      status: 'ACTIVE',
      rating: 5.0,
      createdAt: now - 2 * DAY,
    },
    {
      id: 'admin1',
      nickname: '平台管理员',
      avatar: '🛡️',
      school: '示范大学',
      campus: 'SOUTH',
      verifyStatus: 'VERIFIED',
      role: 'ADMIN',
      status: 'ACTIVE',
      rating: 5.0,
      createdAt: now - 365 * DAY,
    },
  ];

  const categories: Category[] = [
    { id: 'c1', name: '数码', icon: '📱', sortOrder: 1 },
    { id: 'c2', name: '图书教材', icon: '📚', sortOrder: 2, hot: true },
    { id: 'c3', name: '生活用品', icon: '🛋️', sortOrder: 3 },
    { id: 'c4', name: '运动户外', icon: '🏀', sortOrder: 4 },
    { id: 'c5', name: '服饰鞋包', icon: '👟', sortOrder: 5 },
    { id: 'c6', name: '美妆个护', icon: '💄', sortOrder: 6 },
    { id: 'c7', name: '票券卡券', icon: '🎫', sortOrder: 7 },
    { id: 'c8', name: '其他闲置', icon: '📦', sortOrder: 8 },
  ];

  // p[i]: title, sellerId, categoryId, price, original, condition, campus, place, imgs, ageMinutes, view, fav
  const p = (
    id: string,
    title: string,
    sellerId: string,
    categoryId: string,
    price: number,
    originalPrice: number,
    conditionLevel: Product['conditionLevel'],
    campus: Product['campus'],
    locationDesc: string,
    description: string,
    images: number,
    ageMin: number,
    viewCount: number,
    favoriteCount: number,
    negotiable = true,
    status: Product['status'] = 'ON_SALE',
  ): Product => ({
    id,
    sellerId,
    title,
    description,
    categoryId,
    price,
    originalPrice,
    conditionLevel,
    campus,
    locationDesc,
    negotiable,
    status,
    images: img(images),
    viewCount,
    favoriteCount,
    createdAt: now - ageMin * MIN,
    updatedAt: now - ageMin * MIN,
  });

  const products: Product[] = [
    p('p1', '《有机化学》第三版 教材', 'u3', 'c2', 30, 68, 'GOOD', 'SOUTH', '南校区图书馆门口',
      '有机化学教材，整体保存较好，有少量学习笔记，不影响阅读。适合本学期相关课程使用。', 2, 35, 142, 12),
    p('p2', '飞利浦护眼台灯 充电款', 'u1', 'c3', 80, 159, 'ALMOST_NEW', 'SOUTH', '南校区宿舍楼下',
      '飞利浦护眼台灯，三档调光，充电式无线，用了一学期几乎全新，宿舍必备。', 3, 120, 230, 25),
    p('p3', '小米 Buds 蓝牙耳机', 'u2', 'c1', 99, 199, 'GOOD', 'NORTH', '北校区食堂大厅',
      '小米蓝牙耳机，音质不错，续航在线，盒子配件都在，正常使用痕迹。', 2, 200, 310, 30),
    p('p4', '考研英语历年真题全套', 'u3', 'c2', 45, 120, 'GOOD', 'SOUTH', '南校区图书馆门口',
      '考研英语真题全套，含解析，做过一部分有笔记，开学季正需要，先到先得。', 3, 60, 188, 41),
    p('p5', '宿舍小冰箱 单门', 'u4', 'c3', 180, 399, 'WORN', 'NORTH', '北校区宿舍区门口',
      '单门小冰箱，制冷正常，有使用痕迹和小磕碰，宿舍放饮料零食很方便，自提。', 2, 1500, 96, 8),
    p('p6', '捷安特山地车', 'u4', 'c4', 250, 600, 'GOOD', 'NORTH', '北校区体育馆门口',
      '捷安特山地车，21速，骑行正常，刹车灵敏，毕业甩卖，校内代步神器。', 3, 2880, 205, 18),
    p('p7', '斯伯丁篮球 7号', 'u2', 'c4', 50, 120, 'ALMOST_NEW', 'NORTH', '北校区体育馆门口',
      '斯伯丁7号篮球，手感好，几乎全新，买了没怎么打，诚意出。', 1, 300, 77, 6),
    p('p8', '雅思核心词汇 乱序版', 'u3', 'c2', 35, 89, 'GOOD', 'SOUTH', '南校区食堂一楼',
      '雅思词汇书，乱序版，划过重点，背完上岸，开学季需求旺。', 2, 90, 121, 22),
    p('p9', '机械键盘 红轴 87键', 'u1', 'c1', 150, 299, 'ALMOST_NEW', 'SOUTH', '南校区教学楼大厅',
      '红轴机械键盘，87键紧凑布局，手感顺滑，几乎全新，打字打游戏都香。', 3, 240, 266, 33),
    p('p10', 'LED 化妆镜 带补光', 'u2', 'c6', 30, 79, 'GOOD', 'NORTH', '北校区宿舍区门口',
      'LED补光化妆镜，三色温，USB供电，宿舍化妆刚需，成色良好。', 2, 420, 88, 14),
    p('p11', '羽毛球拍 一对', 'u4', 'c4', 90, 220, 'GOOD', 'NORTH', '北校区体育馆门口',
      '羽毛球拍一对，碳素杆，已穿线，附拍包和3个球，适合校内约球。', 2, 360, 64, 9),
    p('p12', '24寸 IPS 显示器', 'u1', 'c1', 450, 799, 'ALMOST_NEW', 'SOUTH', '南校区教学楼大厅',
      '24寸IPS显示器，1080P，无亮点无划痕，自带HDMI线，几乎全新，搬寝出。', 3, 720, 401, 52),
    p('p13', '优衣库羽绒服 男 M', 'u4', 'c5', 150, 499, 'GOOD', 'NORTH', '北校区食堂大厅',
      '优衣库轻型羽绒服，男款M码，黑色百搭，洗过一次成色良好，冬天保暖。', 2, 2160, 133, 11),
    p('p14', '小型电饭煲 1.6L', 'u3', 'c3', 70, 159, 'GOOD', 'SOUTH', '南校区宿舍楼下',
      '1.6L迷你电饭煲，煮饭煮粥都行，1-2人份，宿舍开小灶利器，功能正常。', 2, 600, 99, 7),
  ];

  // 收藏：u1 收藏了几件别人的商品
  const favorites: Favorite[] = [
    { id: 'f1', userId: 'u1', productId: 'p1', createdAt: now - 30 * MIN },
    { id: 'f2', userId: 'u1', productId: 'p4', createdAt: now - 50 * MIN },
    { id: 'f3', userId: 'u1', productId: 'p6', createdAt: now - 2 * HOUR },
  ];

  // 会话：u1（买家）与卖家就某商品沟通
  const sessions: ChatSession[] = [
    {
      id: 's1',
      productId: 'p1',
      buyerId: 'u1',
      sellerId: 'u3',
      lastMessage: '可以的，那就图书馆门口见～',
      lastMessageTime: now - 12 * MIN,
      status: 'ACTIVE',
      buyerUnread: 0,
      sellerUnread: 1,
    },
    {
      id: 's2',
      productId: 'p3',
      buyerId: 'u1',
      sellerId: 'u2',
      lastMessage: '耳机还在吗？能小刀吗？',
      lastMessageTime: now - 3 * HOUR,
      status: 'ACTIVE',
      buyerUnread: 1,
      sellerUnread: 0,
    },
  ];

  const messages: Message[] = [
    { id: 'm1', sessionId: 's1', senderId: 'u1', type: 'TEXT', content: '同学你好，这本有机化学还在吗？', createdAt: now - 40 * MIN },
    { id: 'm2', sessionId: 's1', senderId: 'u3', type: 'TEXT', content: '在的，95新，有一点笔记', createdAt: now - 38 * MIN },
    { id: 'm3', sessionId: 's1', senderId: 'u1', type: 'TEXT', content: '什么时候方便交易？可以在图书馆门口吗？', createdAt: now - 20 * MIN },
    {
      id: 'm4',
      sessionId: 's1',
      senderId: 'u3',
      type: 'APPOINTMENT',
      content: '',
      appointment: { place: '南校区图书馆门口', time: '今天 18:30', status: 'PROPOSED' },
      createdAt: now - 15 * MIN,
    },
    { id: 'm5', sessionId: 's1', senderId: 'u1', type: 'TEXT', content: '可以的，那就图书馆门口见～', createdAt: now - 12 * MIN },
    { id: 'm6', sessionId: 's2', senderId: 'u1', type: 'TEXT', content: '耳机还在吗？能小刀吗？', createdAt: now - 3 * HOUR },
  ];

  const verifications: Verification[] = [
    {
      id: 'v_u5',
      userId: 'u5',
      school: '示范大学',
      campus: 'NORTH',
      studentNo: '2024NORTH500',
      status: 'PENDING',
      createdAt: now - 2 * DAY,
    },
  ];

  const reports: Report[] = [
    {
      id: 'r1',
      reporterId: 'u2',
      targetType: 'PRODUCT',
      targetId: 'p5',
      reason: '价格欺诈',
      description: '描述与实物不符，成色比写的差很多。',
      status: 'PENDING',
      createdAt: now - 5 * HOUR,
    },
  ];

  return { users, categories, products, favorites, sessions, messages, verifications, reports };
}
