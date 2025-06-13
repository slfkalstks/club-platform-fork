package kc.ac.uc.clubplatform.models

data class Department(
    val majorName: String = "",
    val majorSeq: String = "",
    val facultyName: String = "" // 학부/단과대학 정보를 저장하는 속성 추가
)