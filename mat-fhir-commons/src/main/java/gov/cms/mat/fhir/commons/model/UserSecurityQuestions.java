package gov.cms.mat.fhir.commons.model;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "USER_SECURITY_QUESTIONS")

@NamedQueries({
        @NamedQuery(name = "UserSecurityQuestions.findAll", query = "SELECT u FROM UserSecurityQuestions u"),
        @NamedQuery(name = "UserSecurityQuestions.findByUserSecurityQuestionsId", query = "SELECT u FROM UserSecurityQuestions u WHERE u.userSecurityQuestionsId = :userSecurityQuestionsId"),
        @NamedQuery(name = "UserSecurityQuestions.findByRowId", query = "SELECT u FROM UserSecurityQuestions u WHERE u.rowId = :rowId"),
        @NamedQuery(name = "UserSecurityQuestions.findByAnswer", query = "SELECT u FROM UserSecurityQuestions u WHERE u.answer = :answer"),
        @NamedQuery(name = "UserSecurityQuestions.findBySalt", query = "SELECT u FROM UserSecurityQuestions u WHERE u.salt = :salt")})
public class UserSecurityQuestions implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    @Column(name = "USER_SECURITY_QUESTIONS_ID")
    private Integer userSecurityQuestionsId;
    @Basic(optional = false)
    @Column(name = "ROW_ID")
    private int rowId;
    @Column(name = "ANSWER")
    private String answer;
    @Column(name = "SALT")
    private String salt;
    @JoinColumn(name = "QUESTION_ID", referencedColumnName = "QUESTION_ID")
    @ManyToOne
    private SecurityQuestions questionId;
    @JoinColumn(name = "USER_ID", referencedColumnName = "USER_ID")
    @ManyToOne(optional = false)
    private User userId;

    public UserSecurityQuestions() {
    }

    public UserSecurityQuestions(Integer userSecurityQuestionsId) {
        this.userSecurityQuestionsId = userSecurityQuestionsId;
    }

    public UserSecurityQuestions(Integer userSecurityQuestionsId, int rowId) {
        this.userSecurityQuestionsId = userSecurityQuestionsId;
        this.rowId = rowId;
    }

    public Integer getUserSecurityQuestionsId() {
        return userSecurityQuestionsId;
    }

    public void setUserSecurityQuestionsId(Integer userSecurityQuestionsId) {
        this.userSecurityQuestionsId = userSecurityQuestionsId;
    }

    public int getRowId() {
        return rowId;
    }

    public void setRowId(int rowId) {
        this.rowId = rowId;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getSalt() {
        return salt;
    }

    public void setSalt(String salt) {
        this.salt = salt;
    }

    public SecurityQuestions getQuestionId() {
        return questionId;
    }

    public void setQuestionId(SecurityQuestions questionId) {
        this.questionId = questionId;
    }

    public User getUserId() {
        return userId;
    }

    public void setUserId(User userId) {
        this.userId = userId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userSecurityQuestionsId != null ? userSecurityQuestionsId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserSecurityQuestions)) {
            return false;
        }
        UserSecurityQuestions other = (UserSecurityQuestions) object;
        if ((this.userSecurityQuestionsId == null && other.userSecurityQuestionsId != null) || (this.userSecurityQuestionsId != null && !this.userSecurityQuestionsId.equals(other.userSecurityQuestionsId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.UserSecurityQuestions[ userSecurityQuestionsId=" + userSecurityQuestionsId + " ]";
    }

}
