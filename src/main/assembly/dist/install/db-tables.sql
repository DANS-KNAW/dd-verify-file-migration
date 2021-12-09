create table expected (
 doi varchar(255) not null,
 expected_path varchar(1024) not null,
 removed_duplicate_file_count integer not null,
 added_during_migration boolean,
 easy_file_id varchar(64),
 fs_rdb_path varchar(1024),
 removed_original_directory boolean,
 removed_thumbnail boolean,
 sha1_checksum varchar(40),
 transformed_name boolean,
 primary key (doi, expected_path, removed_duplicate_file_count)
);
GRANT INSERT, SELECT, UPDATE, DELETE ON expected TO dd_verify_file_migration;

CREATE TABLE IF NOT EXISTS actual (
    doi VARCHAR(100) NOT NULL,
    sha1_checksum CHAR(40) NOT NULL,
    actual_path VARCHAR(1000),
    storage_id VARCHAR(60) NOT NULL,
    version_nr INTEGER NOT NULL,
    PRIMARY KEY (doi, actual_path, version_nr));
GRANT INSERT, SELECT, UPDATE, DELETE ON actual TO dd_verify_file_migration;
