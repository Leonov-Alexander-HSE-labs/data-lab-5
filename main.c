#include <stdio.h>
#include <string.h>
#include <stdlib.h>

EXEC SQL INCLUDE sqlca;

int main() {
    EXEC SQL BEGIN DECLARE SECTION;
    char dbname[256] = "links_db";
    char username[256];
    char password[256];
    char is_admin[2];
    EXEC SQL END DECLARE SECTION;

    int choice;
    char buffer[1024];
    int is_admin_flag = 0;

    printf("Enter username: ");
    fgets(username, sizeof(username), stdin);
    username[strcspn(username, "\n")] = '\0';

    printf("Enter password: ");
    fgets(password, sizeof(password), stdin);
    password[strcspn(password, "\n")] = '\0';

    EXEC SQL CONNECT TO links_db@localhost USER :username USING :password;
    if (sqlca.sqlcode != 0) {
        fprintf(stderr, "Connection error: %s\n", sqlca.sqlerrm.sqlerrmc);
        return 1;
    }

    EXEC SQL SELECT pg_has_role(CURRENT_USER, 'admin', 'MEMBER') INTO :is_admin;
    is_admin_flag = (is_admin[0] == 't') ? 1 : 0;

    do {
        if (is_admin_flag) {
            printf("\nAdmin Menu:\n");
            printf("1. Create User\n2. Drop Table\n3. Clear Table\n4. Add Link\n");
            printf("5. Update Link\n6. Delete by Alias\n7. Search\n0. Exit\n");
        } else {
            printf("\nGuest Menu:\n");
            printf("1. Search\n0. Exit\n");
        }
        printf("Choice: ");
        fgets(buffer, sizeof(buffer), stdin);
        choice = atoi(buffer);

        EXEC SQL BEGIN DECLARE SECTION;
        char new_user[256], new_pass[256];
        int admin_flag;
        char original_text[1024], alias_text[256];
        char search_text[1024];
        long link_id;
        char new_original[1024], new_alias[256];
        char delete_alias[256];

        struct {
            long id;
            char original[1024];
            char alias[256];
            char created_at[64];
        } links[100];
        EXEC SQL END DECLARE SECTION;

        switch (choice) {
            case 1:
                if (is_admin_flag) {
                    printf("New username: ");
                    fgets(new_user, sizeof(new_user), stdin);
                    new_user[strcspn(new_user, "\n")] = '\0';

                    printf("New password: ");
                    fgets(new_pass, sizeof(new_pass), stdin);
                    new_pass[strcspn(new_pass, "\n")] = '\0';

                    printf("Is admin (1/0): ");
                    fgets(buffer, sizeof(buffer), stdin);
                    admin_flag = atoi(buffer);

                    EXEC SQL CALL create_user(:new_user, :new_pass, :admin_flag);
                    EXEC SQL COMMIT;

                    if (sqlca.sqlcode == 0) {
                        printf("User created successfully.\n");
                    } else {
                        fprintf(stderr, "Error creating user: Code %d, Message: %s\n", (int)sqlca.sqlcode, sqlca.sqlerrm.sqlerrmc);
                    }
                }
                break;

            case 2:
                if (is_admin_flag) {
                    EXEC SQL CALL drop_table();
                    EXEC SQL COMMIT;
                    printf("Table dropped successfully.\n");
                }
                break;

            case 3:
                if (is_admin_flag) {
                    EXEC SQL CALL clear_table();
                    EXEC SQL COMMIT;
                    printf("Table cleared successfully.\n");
                }
                break;

            case 4:
                if (is_admin_flag) {
                    printf("Original URL: ");
                    fgets(original_text, sizeof(original_text), stdin);
                    original_text[strcspn(original_text, "\n")] = '\0';

                    printf("Alias: ");
                    fgets(alias_text, sizeof(alias_text), stdin);
                    alias_text[strcspn(alias_text, "\n")] = '\0';

                    EXEC SQL CALL add_link(:original_text, :alias_text);
                    EXEC SQL COMMIT;

                    if (sqlca.sqlcode == 0) {
                        printf("Link added successfully.\n");
                    } else {
                        fprintf(stderr, "Error adding link: %s\n", sqlca.sqlerrm.sqlerrmc);
                    }
                }
                break;

            case 5:
                if (is_admin_flag) {
                    printf("Link ID: ");
                    fgets(buffer, sizeof(buffer), stdin);
                    link_id = atol(buffer);

                    printf("New Original URL: ");
                    fgets(new_original, sizeof(new_original), stdin);
                    new_original[strcspn(new_original, "\n")] = '\0';

                    printf("New Alias: ");
                    fgets(new_alias, sizeof(new_alias), stdin);
                    new_alias[strcspn(new_alias, "\n")] = '\0';

                    EXEC SQL CALL update_link(:link_id, :new_original, :new_alias);
                    EXEC SQL COMMIT;

                    if (sqlca.sqlcode == 0) {
                        printf("Link updated successfully.\n");
                    } else {
                        fprintf(stderr, "Error updating link: %s\n", sqlca.sqlerrm.sqlerrmc);
                    }
                }
                break;

            case 6:
                if (is_admin_flag) {
                    printf("Alias to delete: ");
                    fgets(delete_alias, sizeof(delete_alias), stdin);
                    delete_alias[strcspn(delete_alias, "\n")] = '\0';

                    EXEC SQL CALL delete_by_alias(:delete_alias);
                    EXEC SQL COMMIT;

                    if (sqlca.sqlcode == 0) {
                        printf("Link deleted successfully.\n");
                    } else {
                        fprintf(stderr, "Error deleting link: %s\n", sqlca.sqlerrm.sqlerrmc);
                    }
                }
                break;

            case 7:
                printf("Search text: ");
                fgets(search_text, sizeof(search_text), stdin);
                search_text[strcspn(search_text, "\n")] = '\0';

                EXEC SQL BEGIN DECLARE SECTION;
                int result_count;
                EXEC SQL END DECLARE SECTION;

                EXEC SQL SELECT COUNT(*) INTO :result_count
                FROM search_by_original(:search_text);

                if (result_count > 0) {
                    EXEC SQL SELECT * INTO :links FROM search_by_original(:search_text);

                    for(int i = 0; i < result_count; i++) {
                        printf("ID: %ld\nOriginal: %s\nAlias: %s\nCreated: %s\n\n",
                               links[i].id, links[i].original, links[i].alias, links[i].created_at);
                    }
                } else {
                    printf("No results found.\n");
                }
                break;

            case 0:
                break;

            default:
                printf("Invalid choice.\n");
        }
    } while (choice != 0);

    EXEC SQL DISCONNECT;
    return 0;
}