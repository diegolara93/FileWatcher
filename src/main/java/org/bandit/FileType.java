package org.bandit;

public enum FileType {
    PDF("pdf"),
    TXT("txt"),
    DOCX("docx"),
    HTML("html"),
    JSON("json");

    private final String extension;

    FileType(String extension) {
        this.extension = extension;
    }

    public String getExtension() {
        return extension;
    }

    public static FileType fromExtension(String extension) {
        for (FileType type : values()) {
            if (type.extension.equalsIgnoreCase(extension)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown file type: " + extension);
    }

    @Override
    public String toString() {
        return "FileType{" +
                "extension='" + extension + '\'' +
                '}';
    }
}
