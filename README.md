## Bảng phân chia công việc trong nhóm
| Thành viên | Công việc |
|:----------:|:---------:|
| Vũ Đức Hoàng | phát triển server core. setup docker, github actions workflow cho CI/CD |
| Phạm Huy Hiêu | viết test kiểm thử cho server core |
| Nguyễn Ngọc Huy | phát triển client core |
| Vũ Đình Giang | phát triển UI/UX cho client |

## Sơ đồ thiết kế lớp
```mermaid
classDiagram
direction TB

namespace Models {
  class Entity {
    - String id
    - long updatedAt
    - long createdAt
  }
  class User {
    - String email
    - String password
    - String username
  }
  class Member {
    # double frozenBalance
    # boolean isBanned
    # double accountBalance
  }
  class Admin
  class Item {
    - String name
    - String ownerId
    - String description
  }
  class Art {
    - int creationYear
    - String artist
  }
  class Electronics {
    - int warrantyMonths
    - String brand
  }
  class Vehicle {
    - String engineType
    - int mileage
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
  class BidTransaction {
    - double amount
    - String auctionId
    - String bidderId
    - boolean isAutoBid
  }
  class AutoBidConfig {
    - double maxBid
    - double increment
    - LocalDateTime registeredAt
    - Member bidder
    - Auction auction
  }
}

namespace Interfaces {
  class IAuctionRepository { <<Interface>> }
  class IAuctionService { <<Interface>> }
  class IAutoBidService { <<Interface>> }
  class IItemRepository { <<Interface>> }
  class IItemService { <<Interface>> }
  class ITransactionRepository { <<Interface>> }
  class IUserRepository { <<Interface>> }
  class IUserService { <<Interface>> }
}

namespace Core {
  class DatabaseConnection {
    - DatabaseConnection instance
    - HikariDataSource dataSource
  }
  class ItemFactory
  class NotificationService
  class AuctionScheduler
}

namespace Repositories {
  class AuctionRepository
  class ItemRepository
  class TransactionRepository
  class UserRepository
}

namespace Services {
  class AuctionService
  class ItemService
  class UserService
}

%% Inheritance
Entity <|-- User
Entity <|-- Item
Entity <|-- Auction
Entity <|-- BidTransaction
Entity <|-- AutoBidConfig
User <|-- Admin
User <|-- Member
Item <|-- Art
Item <|-- Electronics
Item <|-- Vehicle

%% Realization
IAuctionRepository <|.. AuctionRepository
IAuctionService <|.. AuctionService
IItemRepository <|.. ItemRepository
IItemService <|.. ItemService
ITransactionRepository <|.. TransactionRepository
IUserRepository <|.. UserRepository
IUserService <|.. UserService

%% Relationships (Associations / Compositions)
Auction *-- Item
Auction *-- Member
AutoBidConfig *-- Auction
AutoBidConfig *-- Member

%% Respository Implementations
AuctionRepository *-- DatabaseConnection
AuctionRepository *-- IItemRepository
AuctionRepository *-- IUserRepository
ItemRepository *-- DatabaseConnection
TransactionRepository *-- DatabaseConnection
UserRepository *-- DatabaseConnection

%% Creation Dependencies
AuctionRepository ..> Auction : create
AuctionService ..> Auction : create
AuctionService ..> BidTransaction : create
ItemFactory ..> Art : create
ItemFactory ..> Electronics : create
ItemFactory ..> Vehicle : create
ItemRepository ..> Art : create
ItemRepository ..> Electronics : create
ItemRepository ..> Vehicle : create
UserRepository ..> Admin : create
UserRepository ..> Member : create

%% Service Dependencies
AuctionScheduler *-- IAuctionService
AuctionService *-- IAuctionRepository
AuctionService *-- IAutoBidService
AuctionService *-- IItemService
AuctionService *-- ITransactionRepository
AuctionService *-- IUserService
AuctionService *-- NotificationService
AuctionService *-- AuctionScheduler
ItemService *-- IItemRepository
UserService *-- IUserRepository
```