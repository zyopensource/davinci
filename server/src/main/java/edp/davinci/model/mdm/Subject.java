package edp.davinci.model.mdm;

import lombok.Data;

/**
 * @author linda
 */
@Data
public class Subject {
    private String ID;
    private String SubjectNumber;
    private String SubjectCode;
    private String SubjectName;
    private String SubjectLongName;
    private String SubjectId;
    private String SuperiorSubjectsId;

    public Subject(String ID, String subjectNumber, String subjectCode, String subjectName, String subjectLongName, String subjectId, String superiorSubjectsId) {
        this.ID = ID;
        SubjectNumber = subjectNumber;
        SubjectCode = subjectCode;
        SubjectName = subjectName;
        SubjectLongName = subjectLongName;
        SubjectId = subjectId;
        SuperiorSubjectsId = superiorSubjectsId;
    }
}

