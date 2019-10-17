/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.cms.mat.fhir.commons.model;

import java.io.Serializable;
import java.util.Collection;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 *
 * @author duanedecouteau
 */
@Entity
@Table(name = "SECURITY_QUESTIONS")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "SecurityQuestions.findAll", query = "SELECT s FROM SecurityQuestions s"),
    @NamedQuery(name = "SecurityQuestions.findByQuestionId", query = "SELECT s FROM SecurityQuestions s WHERE s.questionId = :questionId"),
    @NamedQuery(name = "SecurityQuestions.findByQuestion", query = "SELECT s FROM SecurityQuestions s WHERE s.question = :question")})
public class SecurityQuestions implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @Basic(optional = false)
    @Column(name = "QUESTION_ID")
    private Integer questionId;
    @Basic(optional = false)
    @Column(name = "QUESTION")
    private String question;
    @OneToMany(mappedBy = "questionId")
    private Collection<UserSecurityQuestions> userSecurityQuestionsCollection;

    public SecurityQuestions() {
    }

    public SecurityQuestions(Integer questionId) {
        this.questionId = questionId;
    }

    public SecurityQuestions(Integer questionId, String question) {
        this.questionId = questionId;
        this.question = question;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    @XmlTransient
    public Collection<UserSecurityQuestions> getUserSecurityQuestionsCollection() {
        return userSecurityQuestionsCollection;
    }

    public void setUserSecurityQuestionsCollection(Collection<UserSecurityQuestions> userSecurityQuestionsCollection) {
        this.userSecurityQuestionsCollection = userSecurityQuestionsCollection;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (questionId != null ? questionId.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof SecurityQuestions)) {
            return false;
        }
        SecurityQuestions other = (SecurityQuestions) object;
        if ((this.questionId == null && other.questionId != null) || (this.questionId != null && !this.questionId.equals(other.questionId))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "gov.cms.mat.fhir.commons.model.SecurityQuestions[ questionId=" + questionId + " ]";
    }
    
}
