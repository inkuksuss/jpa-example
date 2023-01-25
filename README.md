# JPA

## @Entity
### @Entity가 붙은 클래스는 JPA가 관리한다

+ JPA를 사용해서 테이블과 매핑할 클래스는 필수
+ 기본 생성자가 필수 (public or protected)
+ final class, enum, interface, inner class는 사용 불가
+ 저장할 필드에 final 사용 불가

+ ### 속성
    + name: 기본 값 = 클래스명
        + 기본 값 사용 권장

## @Table
### @Table은 엔티티와 매핑할 테이블 지정

+ ### 속성
    + name: 매핑할 테이블 이름
        + default => Entity name
    + catalog: DB catalog 매핑
    + schema: DB schema 매핑
    + uniqueConstraints: DDL 생성 시 유니크 제약 조건 생성

## DB 스키마 자동 생성
````java
<property name="hibernate.hbm2ddl.auto" value="create"/>
````
+ ### 속성
    + create: drap + create
    + create-drop: create와 같으나 종료 시 drop
    + update: 변경분만 반영
    + validate: entity와 table이 정상 매핑 되었는지만 확인
    + none: 사용하지 않음
#### -> 운영 환경에서는 none || validate 사용 필수 (검증되지 않은 테이블 수정 위험)


## Column 매핑
### Example
````java
@Entity
public class Member {

    @Id // PK
    private Long id;

    @Column(name = "name") // 특정 Column과 매핑
    private String username;

    private Integer age;

    @Enumerated(EnumType.STRING) // Enum 사용
    private RoleType roleType;

    @Temporal(TemporalType.TIMESTAMP) // 날짜 사용
    private Date createdDate;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModifiedDate;

    @Lob // 큰 데이터를 저장할 때 사용 like text
    private String description;

    @Transient // 특정 필드를 매핑하고 싶지 않을 때
    private int temp;

    public Member() {
    }

    public Member(Long id, String username) {
        this.id = id;
        this.username = username;
    }
}
````

## @Column
+ ### 속성
    + name: 필드와 매핑할 테이블 컬럼명
    + insertable, updatable: 등록, 변경 여부
        + Default => true
    + nullable: null 값 허용 여부 - not null
    + unique: @Table의 unique와 같지만 한 Column에 간단히 유니크 제약을 검
        + unique 명이 알 수 없게 들어가므로 권장 X
    + columnDefinition: DB 컬럼 정보를 직접 줌
        + ex) columnDefinition = `varchar(100) default 'EMPTY'`
    + length: 문자 길이 제약, String에만 사용
        + Default => 255
    + precision, scale: BigDecimal or BigInteger 타입 사용
        + 큰 숫자나 정밀한 소수를 다룰 때 사용
        + precision => 소수점을 포함한 전체 자릿수
        + scale => 소수의 자릿수
        + double, float 타입에 적용 X

## @Enumerated
### Enum 타입을 매핑할 때 사용
+ ### 속성
    + EnumType.ORDINAL: enum의 순서를 저장
        + #### Default이지만 Enum 순서 변경 시 문제가 됨으로 사용 X
    + EnumType.String: enum의 이름을 저장

## @Temporal
### 날짜 타입을 매핑할 때 사용
#### -> LocalDate, LocalDateTime을 사용할 때는 사용하지 않아도 됨
+ ### 속성
    + DATE, TIME, TIMESTAMP

## @Lob
### DB BLOB, CLOB 타입과 매핑
#### 매핑하는 필드 타입이 문자면 CLOB(String, char[], CLOB), 나머지는 BLOB

## @Transient
### 필드 매핑 X, DB 저장 및 조회 X

## PK 매핑
### @Id
+ 직접 할당
### @GeneratedValue
+ IDENTITY: DB에 위임
+ SEQUENCE: DB의 시퀀스 오브젝트 사용
    + @SequenceGenerator 필요
+ TABLE: 키 생성용 테이블 사용
    + @TableGenerator 필요
+ AUTO: 방언에 따라 자동 지정

### IDENTITY
+ 기본키 생성을 DB에 위임
+ 주로 MySQL, PostgreSQL, DB2에서 사용
    + ex) MySQL AUTO_INCREMENT
+ JPA는 주로 트랜잭션 커밋 시점에 INSERT를 날리지만 그 경우 PK를 알 수 없기에 em.persist() 시점에 INSERT 실행

````java
@Entity
public class Member {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;
}
````

### SEQUENCE
+ 유일한 값을 순서대로 생성하는 DB 오브젝트
+ Oracle, PostgreSQL, DB2, H2 등에서 사용


+ #### 속성
    + name: 식별자 생성기 이름 => 필수
    + sequenceName: DB에 등록되어 있는 시퀀스 이름
        + Default = hibernate_sequence
    + initialValue: DDL 생성 시에만 사용, 처음 시작 하는 수
    + allocationSize: 시퀀스 한 번 호출에 증가하는 수
        + ***DB 시퀀스 값이 하니씩 증가한다면 반드시 1로 설정***
        + Default = 50
    + catalog, schema: DB catalog, schema 이름

````java
@Entity
@SequenceGenerator(
name = "MEMBER_SEQ_GENERATOR",
sequenceName = "MEMBER_SEQ", //매핑할 데이터베이스 시퀀스 이름
initialValue = 1, allocationSize = 1)
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE,
            generator = "MEMBER_SEQ_GENERATOR")
    private Long id;
}
````

### @TABLE
+ 키 생성 전용 테이블을 만들어 DB 시퀀스를 흉내내는 전략
    + 모든 DB에 적용 가능하나 성능 저하

````sql
create table MY_SEQUENCES (
    sequence_name varchar(255) not null,
    next_val bigint,
    primary key ( sequence_name )
)
````

````java
@Entity
@TableGenerator(
name = "MEMBER_SEQ_GENERATOR",
table = "MY_SEQUENCES",
pkColumnValue = "MEMBER_SEQ", allocationSize = 1)
public class Member{
        @Id
        @GeneratedValue(strategy = GenerationType.TABLE,
                generator = "MEMBER_SEQ_GENERATOR")
        private Long id;
}
````

+ ### 속성
    + name: 식별자 생성기 이름 => 필수
    + table: 키생성 테이블명
        + Default = hibernate_sequences
    + pkColumnName: 시퀀스 컬럼명
        + Default = sequence_name
    + valueColumnNa: 시퀀스 값 컬럼명
        + Default = next_val
    + pkColumnValue: 키로 사용할 값 이름
        + Default = entity name
    + initialValue: 초기 값, 마지막으로 생성된 값 기준
        + Default = 0
    + ***allocationSize: 시퀀스 한 번 호출에 증가하는 수***
        + 성능 최적화, 동시성 이슈 X
        + Default = 50
    + catalog, schema: DB catalog, schema 이름
    + uniqueConstraint: 유니크 제약 조건 지정


## 연관관계 맵핑
### 객체 맵핑

+ JPA 양방향 관계는 서로 다른 단방향 관계이다.

### 1. 단방향일 때
````java
@Entity
public class Member {

    @Id
    @GeneratedValue
    @Column(name = "MEMBER_ID")
    private Long id;

    @Column(name = "USERNAME")
    private String name;

    @ManyToOne // 외래키를 가지고 있는 쪽 => 연관관계 주인
    @JoinColumn(name = "TEAM_ID") 
    private Team team;


    public Member() {
    }
}
````

### 2. 양방향을 추가할 때
+ 주인이 아닌쪽은 읽기만 가능
+ 주인이 아니면 mappedBy 속성으로 주인 지정
````java
@Entity
public class Team {

    @Id
    @GeneratedValue
    @Column(name = "TEAM_ID")
    private Long id;

    @Column(name = "TEAM_NAME")
    private String name;

    @OneToMany(mappedBy = "team")
    private List<Member> members = new ArrayList<>();
}

````

### 실습
````java
    public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
        EntityManager em = emf.createEntityManager();
        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Team team = new Team();
            team.setName("TeamA");
            em.persist(team);


            Member member = new Member();
            member.setName("member1");
            member.setTeam(team);
            em.persist(member);

            em.flush();
            em.clear();
            
            Team findTeam = em.find(Team.class, team.getId());
            List<Member> members = findTeam.getMembers();
            for (Member m : members) {
                System.out.println("m = " + m.getName());
            }


            Member findMember = em.find(Member.class, member.getId());

            tx.commit();
        } catch (Exception e) {
            e.printStackTrace();
            tx.rollback();
        } finally {
            em.close();
        }
        emf.close();
    }
}
````

+ em.flush() + em.clear()를 호출하지 않으면 1차 캐쉬에 있는 값으로 인해 Member.class의 team과 Team.class의 members는 값을 가지지 않는다.
  -> ***연관관계 편의 메서드를 통해 해결***

ex) 한쪽에서 양쪽 모두의 값을 바꿔준다. -> 중복 제외 같은 추가적인 로직 필요
````java
public void changeTeam(Team team) {
        this.team = team;
        team.getMembers().add(this);
    }
````

+ toString(), lombok, JSON 생성 라이브러리 사용 시 무한루프 위험
-> ***Controller에서 Entity를 반환하지 말고 DTO로 변환 후 전송 해야함***
  
### ***설계 단계에서는 단방향 관계을 우선 사용하고 개발 단계에서 필요하다면 양방향 관계 추가***