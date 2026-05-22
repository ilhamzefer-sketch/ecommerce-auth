# Marketplace Ecommerce Microservice Plani

Bu plan userlerin oz mehsullarini yerlesdirib sata bildiyi marketplace ecommerce sayti ucundur. Meqsed prod seviyyesinde duzgun domen bolgusu, olculebilme, tehlukesizlik ve sonradan rahat inkisafdir.

## 1. Umumi Arxitektura

Tovsiyye olunan struktur:

- Frontend Web App
- API Gateway / BFF
- Core microservice-ler
- Async event sistemi
- Her service ucun ayrica database
- Observability ve DevOps qatlari

Tovsiyye olunan kommunikasiya:

- User-facing istekler: HTTP/REST ve ya GraphQL API Gateway uzerinden
- Service-to-service: REST/gRPC
- Asinxron prosesler: Kafka, RabbitMQ ve ya NATS
- File/media: S3 compatible storage
- Cache: Redis

## 2. MVP Ucun Lazim Olan Ilk Microservice-ler

Bu service-lerle marketplace-in ilk isleyen versiyasini qurmaq olar.

### 2.1 ecommerce-auth-service

Mesuliyyet:

- Register/login/logout
- JWT ve refresh token
- Password reset
- Email verification
- Role ve permission temeli
- Seller/user/admin rollari

Qeyd: Ilk baslanacaq en dogru service budur, cunki diger service-ler user identity ve permission-lara baglidir.

Database:

- PostgreSQL

Esas entity-ler:

- User
- Role
- Permission
- RefreshToken
- EmailVerificationToken
- PasswordResetToken

### 2.2 ecommerce-user-service

Mesuliyyet:

- User profili
- Seller profili
- Address book
- User settings
- KYC/verification status ucun temel struktur

Database:

- PostgreSQL

Esas entity-ler:

- UserProfile
- SellerProfile
- Address
- UserPreference

### 2.3 ecommerce-catalog-service

Mesuliyyet:

- Mehsul yaratmaq, yenilemek, silmek
- Category ve brand idaresi
- Product variantlari
- Product attributes
- Product status: draft, pending_review, active, rejected, archived

Database:

- PostgreSQL

Esas entity-ler:

- Product
- ProductVariant
- Category
- Brand
- ProductAttribute
- ProductImage

### 2.4 ecommerce-inventory-service

Mesuliyyet:

- Stok sayi
- Stok rezervasiyasi
- Order yarananda stok azaltmaq
- Order legv olunanda stok qaytarmaq

Database:

- PostgreSQL

Esas entity-ler:

- InventoryItem
- StockReservation
- StockMovement

### 2.5 ecommerce-cart-service

Mesuliyyet:

- Cart yaratmaq
- Cart-a product elave etmek/silmek
- Quantity deyismek
- Guest cart ve logged-in cart merge

Database:

- Redis ve ya PostgreSQL

Esas entity-ler:

- Cart
- CartItem

### 2.6 ecommerce-order-service

Mesuliyyet:

- Checkout baslatmaq
- Order yaratmaq
- Order status lifecycle
- Seller bazasinda order item bolgusu
- Order cancellation

Database:

- PostgreSQL

Esas entity-ler:

- Order
- OrderItem
- OrderStatusHistory
- OrderPaymentSnapshot
- OrderShippingSnapshot

### 2.7 ecommerce-payment-service

Mesuliyyet:

- Payment intent yaratmaq
- Payment provider inteqrasiyasi
- Refund
- Seller payout ucun payment eventleri
- Webhook qebulu

Database:

- PostgreSQL

Esas entity-ler:

- Payment
- PaymentTransaction
- Refund
- PaymentProviderWebhook

### 2.8 ecommerce-shipping-service

Mesuliyyet:

- Delivery address validation
- Shipping method secimi
- Shipping price hesablanmasi
- Tracking number
- Shipment status

Database:

- PostgreSQL

Esas entity-ler:

- Shipment
- ShippingMethod
- TrackingEvent

## 3. Prod Seviyye Ucun Elave Microservice-ler

MVP-den sonra asagidaki service-ler sistemi daha professional edir.

### 3.1 ecommerce-search-service

Mesuliyyet:

- Product search
- Filter/sort
- Full-text search
- Faceted search

Texnologiya:

- Elasticsearch ve ya OpenSearch

Data source:

- Catalog eventleri ile index yenilenir

### 3.2 ecommerce-review-service

Mesuliyyet:

- Product review
- Seller rating
- Review moderation
- Verified purchase review

Database:

- PostgreSQL

### 3.3 ecommerce-notification-service

Mesuliyyet:

- Email notification
- SMS notification
- Push notification
- Template idaresi

Eventlerden dinleyir:

- UserRegistered
- OrderCreated
- PaymentSucceeded
- ShipmentUpdated

### 3.4 ecommerce-media-service

Mesuliyyet:

- Product image upload
- Image resize/compress
- File validation
- CDN URL yaratmaq

Storage:

- S3 compatible storage
- CDN

### 3.5 ecommerce-admin-service

Mesuliyyet:

- Admin panel API-lari
- Product moderation
- User/seller moderation
- Dispute ve report-lara baxmaq

### 3.6 ecommerce-dispute-service

Mesuliyyet:

- Buyer-seller dispute
- Return request
- Complaint
- Admin decision flow

### 3.7 ecommerce-payout-service

Mesuliyyet:

- Seller balans
- Commission hesablanmasi
- Payout request
- Payout status

### 3.8 ecommerce-promotion-service

Mesuliyyet:

- Coupon
- Discount
- Campaign
- Seller-specific promotion

### 3.9 ecommerce-analytics-service

Mesuliyyet:

- Sales analytics
- Seller dashboard metricleri
- Product view/click/order metricleri
- Funnel analytics

### 3.10 ecommerce-audit-service

Mesuliyyet:

- Critical action log
- Admin action history
- Security audit
- Compliance ucun event saxlanmasi

## 4. Tovsiyye Olunan Service Adlari

Core:

- ecommerce-api-gateway
- ecommerce-auth-service
- ecommerce-user-service
- ecommerce-catalog-service
- ecommerce-inventory-service
- ecommerce-cart-service
- ecommerce-order-service
- ecommerce-payment-service
- ecommerce-shipping-service

Growth/prod:

- ecommerce-search-service
- ecommerce-review-service
- ecommerce-notification-service
- ecommerce-media-service
- ecommerce-admin-service
- ecommerce-dispute-service
- ecommerce-payout-service
- ecommerce-promotion-service
- ecommerce-analytics-service
- ecommerce-audit-service

Platform:

- ecommerce-config-service
- ecommerce-service-discovery
- ecommerce-observability-stack
- ecommerce-message-broker

## 5. Ilk Baslama Ardicitligi

En praktik sira:

1. ecommerce-auth-service
2. ecommerce-user-service
3. ecommerce-catalog-service
4. ecommerce-media-service
5. ecommerce-inventory-service
6. ecommerce-cart-service
7. ecommerce-order-service
8. ecommerce-payment-service
9. ecommerce-shipping-service
10. ecommerce-notification-service
11. ecommerce-search-service
12. ecommerce-admin-service

Sebeb:

- Auth olmadan diger service-lerde ownership ve permission yoxlamaq cetindir.
- Catalog ve media marketplace-in esasidir.
- Inventory, cart ve order birlikde checkout flow yaradir.
- Payment ve shipping order-den sonra baglanir.
- Search ve admin MVP-ni guclendirir, amma ilk gun bloklayici deyil.

## 6. Esas Eventler

Event-driven arxitektura ucun baslangic event siyahisi:

- UserRegistered
- UserEmailVerified
- SellerProfileCreated
- ProductCreated
- ProductUpdated
- ProductApproved
- ProductRejected
- ProductDeleted
- InventoryReserved
- InventoryReservationFailed
- InventoryReleased
- CartCheckedOut
- OrderCreated
- OrderCancelled
- PaymentInitiated
- PaymentSucceeded
- PaymentFailed
- RefundRequested
- RefundSucceeded
- ShipmentCreated
- ShipmentStatusChanged
- ReviewCreated
- PayoutCalculated
- PayoutPaid

## 7. Database Prinsipleri

- Her microservice oz database-ine sahib olsun.
- Bir service basqa service-in database-ine direct qosulmasin.
- Cross-service data lazim olanda API ve ya event istifade olunsun.
- Order kimi kritik proseslerde snapshot saxlanilsin.
- Distributed transaction yerine saga pattern istifade olunsun.

## 8. Security Prinsipleri

- API Gateway-de token validation
- Service daxilinde permission check
- Seller yalniz oz product/order melumatlarini deyise bilsin
- Admin action-lar audit log-a yazilsin
- Payment webhook-lar signature ile yoxlanilsin
- File upload ucun size/type validation
- Rate limiting
- Refresh token rotation

## 9. DevOps ve Prod Hazirliq

Lazim olanlar:

- Dockerfile her service ucun
- docker-compose local development ucun
- Kubernetes prod deployment ucun
- CI/CD pipeline
- Centralized logging
- Metrics
- Distributed tracing
- Health check endpointleri
- Readiness/liveness probe
- Secrets management

Tovsiyye olunan stack:

- Backend: Java Spring Boot, .NET, NestJS ve ya Go
- DB: PostgreSQL
- Cache: Redis
- Broker: Kafka ve ya RabbitMQ
- Search: OpenSearch
- Object storage: MinIO local, S3 prod
- Observability: Prometheus, Grafana, Loki, OpenTelemetry

## 10. MVP Scope

Ilk real versiyada bunlar olsun:

- User register/login
- Seller profile yaratmaq
- Product yaratmaq ve sekil yuklemek
- Product approve/admin moderation
- Product list/detail
- Cart
- Checkout
- Order yaratmaq
- Fake/sandbox payment
- Basic shipping status
- Email notification
- Admin panel ucun basic API

## 11. Son Qerar

Ilk microservice ecommerce-auth-service olmalidir.

Sonra ecommerce-user-service ve ecommerce-catalog-service yazilmalidir. Bu uc service marketplace-in temelini yaradir: kim satir, ne satir, kim ala bilir.
