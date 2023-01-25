package jpashop;

import jpashop.domain.Member;
import jpashop.domain.Team;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import java.util.List;

public class JpaMain {

    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

        EntityManager em = emf.createEntityManager();

        EntityTransaction tx = em.getTransaction();
        tx.begin();

        try {
            Team team = new Team();
            team.setName("TeamA");
//            team.getMembers().add(member);
            em.persist(team);


            Member member = new Member();
            member.setName("member1");
            member.setTeam(team);
            em.persist(member);

//            em.flush();
//            em.clear();

            System.out.println("============== ");

            Team findTeam = em.find(Team.class, team.getId());
            List<Member> members = findTeam.getMembers();
            for (Member m : members) {
                System.out.println("m = " + m.getName());
            }


            Member findMember = em.find(Member.class, member.getId());
//            List<Member> members = findMember.getTeam().getMembers();

//            for (Member m : members) {
//                System.out.println("m = " + m.getName());
//            }
//            Team findTeam = findMember.getTeam();
//            System.out.println("findTeam = " + findTeam.getName());

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
