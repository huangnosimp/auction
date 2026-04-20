## Bảng phân chia công việc trong nhóm
| Thành viên | Công việc |
|:----------:|:---------:|
| Vũ Đức Hoàng | phát triển server core. setup docker, github actions workflow cho CI/CD |
| Phạm Huy Hiêu | viết test kiểm thử cho server core |
| Nguyễn Ngọc Huy | phát triển client core |
| Vũ Đình Giang | phát triển UI/UX cho client |

## Sơ đồ thiết kế lớp
```
mermaid
graphTD
classDiagram
direction BT
class Admin
class Art {
  - int creationYear
  - String artist
}
class Auction {
  - long startTime
  - double currentPrice
  - double startPrice
  - AuctionStatus status
  - long endTime
  - String currentWinnerId
  - Item item
  - ConcurrentHashMap~String, Member~ bidders
  - Member seller
}
class AuctionRepository {
  - DatabaseConnection databaseConnection
  - IUserRepository userRepository
  - IItemRepository itemRepository
}
class AuctionScheduler {
  - ConcurrentHashMap~String, ScheduledFuture~?~~ endTimer
  - ScheduledExecutorService scheduler
  - ConcurrentHashMap~String, ScheduledFuture~?~~ startTimer
  - IAuctionService auctionService
}
class AuctionService {
  - ITransactionRepository transactionRepository
  - IAuctionRepository auctionRepository
  - ConcurrentHashMap~String, Object~ auctionLocks
  - NotificationService notificationService
  - AuctionScheduler scheduler
  - IItemService itemService
  - IAutoBidService autoBidService
  - IUserService userService
}
class AutoBidConfig {
  - double maxBid
  - double increment
  - LocalDateTime registeredAt
  - Member bidder
  - Auction auction
}
class BidTransaction {
  - double amount
  - String auctionId
  - String bidderId
  - boolean isAutoBid
}
class DatabaseConnection {
  - DatabaseConnection instance
  - HikariDataSource dataSource
}
class Electronics {
  - int warrantyMonths
  - String brand
}
class Entity {
  - String id
  - long updatedAt
  - long createdAt
}
class IAuctionRepository {
<<Interface>>

}
class IAuctionService {
<<Interface>>

}
class IAutoBidService {
<<Interface>>

}
class IItemRepository {
<<Interface>>

}
class IItemService {
<<Interface>>

}
class ITransactionRepository {
<<Interface>>

}
class IUserRepository {
<<Interface>>

}
class IUserService {
<<Interface>>

}
class Item {
  - String name
  - String ownerId
  - String description
}
class ItemFactory
class ItemRepository {
  - DatabaseConnection databaseConnection
}
class ItemService {
  - IItemRepository itemRepository
}
class Member {
  # double frozenBalance
  # boolean isBanned
  # double accountBalance
}
class NotificationService
class TransactionRepository {
  - DatabaseConnection databaseConnection
}
class User {
  - String email
  - String password
  - String username
}
class UserRepository {
  - DatabaseConnection databaseConnection
}
class UserService {
  - IUserRepository userRepository
}
class Vehicle {
  - String engineType
  - int mileage
}

Admin  -->  User
Art  -->  Item
Auction  -->  Entity
Auction "1" *--> "item 1" Item
Auction "1" *--> "bidders *" Member
AuctionRepository  ..>  Auction : «create»
AuctionRepository "1" *--> "databaseConnection 1" DatabaseConnection
AuctionRepository  ..>  IAuctionRepository
AuctionRepository "1" *--> "itemRepository 1" IItemRepository
AuctionRepository "1" *--> "userRepository 1" IUserRepository
AuctionScheduler "1" *--> "auctionService 1" IAuctionService
AuctionService  ..>  Auction : «create»
AuctionService "1" *--> "scheduler 1" AuctionScheduler
AuctionService  ..>  BidTransaction : «create»
AuctionService "1" *--> "auctionRepository 1" IAuctionRepository
AuctionService  ..>  IAuctionService
AuctionService "1" *--> "autoBidService 1" IAutoBidService
AuctionService "1" *--> "itemService 1" IItemService
AuctionService "1" *--> "transactionRepository 1" ITransactionRepository
AuctionService "1" *--> "userService 1" IUserService
AuctionService "1" *--> "notificationService 1" NotificationService
AutoBidConfig "1" *--> "auction 1" Auction
AutoBidConfig  -->  Entity
AutoBidConfig "1" *--> "bidder 1" Member
BidTransaction  -->  Entity
Electronics  -->  Item
Item  -->  Entity
ItemFactory  ..>  Art : «create»
ItemFactory  ..>  Electronics : «create»
ItemFactory  ..>  Vehicle : «create»
ItemRepository  ..>  Art : «create»
ItemRepository "1" *--> "databaseConnection 1" DatabaseConnection
ItemRepository  ..>  Electronics : «create»
ItemRepository  ..>  IItemRepository
ItemRepository  ..>  Vehicle : «create»
ItemService "1" *--> "itemRepository 1" IItemRepository
ItemService  ..>  IItemService
Member  -->  User
TransactionRepository "1" *--> "databaseConnection 1" DatabaseConnection
TransactionRepository  ..>  ITransactionRepository
User  -->  Entity
UserRepository  ..>  Admin : «create»
UserRepository "1" *--> "databaseConnection 1" DatabaseConnection
UserRepository  ..>  IUserRepository
UserRepository  ..>  Member : «create»
UserService "1" *--> "userRepository 1" IUserRepository
UserService  ..>  IUserService
Vehicle  -->  Item
```