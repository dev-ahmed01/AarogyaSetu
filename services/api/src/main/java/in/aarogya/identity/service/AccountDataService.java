package in.aarogya.identity.service;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import in.aarogya.identity.domain.UserRole;
import in.aarogya.identity.repository.UserAccountRepository;
import in.aarogya.security.SecurityAuditService;

@Service
public class AccountDataService {

    private final UserAccountRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;
    private final SecurityAuditService auditService;

    public AccountDataService(
        UserAccountRepository userRepository,
        PasswordEncoder passwordEncoder,
        JdbcTemplate jdbcTemplate,
        SecurityAuditService auditService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
        this.auditService = auditService;
    }

    @Transactional
    public Map<String, Object> export(UUID userId) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new BadCredentialsException(
                "Account not found."
            ));

        var data = new LinkedHashMap<String, Object>();
        data.put("exportedAt", Instant.now());
        data.put("formatVersion", 1);

        data.put(
            "account",
            jdbcTemplate.queryForMap(
                """
                SELECT id, email, display_name, role, enabled, created_at, updated_at
                FROM user_accounts
                WHERE id = ?
                """,
                userId
            )
        );

        data.put(
            "profile",
            jdbcTemplate.queryForList(
                """
                SELECT age_years, sex_for_nutrition, height_cm, weight_kg,
                       activity_level, dietary_pattern, state_or_region,
                       onboarding_completed_at, created_at, updated_at
                FROM health_profiles
                WHERE user_id = ?
                """,
                userId
            )
        );

        data.put(
            "profileGoals",
            jdbcTemplate.queryForList(
                "SELECT goal_code FROM profile_goals WHERE user_id = ? ORDER BY goal_code",
                userId
            )
        );
        data.put(
            "profileAllergies",
            jdbcTemplate.queryForList(
                "SELECT allergy_code FROM profile_allergies WHERE user_id = ? ORDER BY allergy_code",
                userId
            )
        );
        data.put(
            "profileHealthContexts",
            jdbcTemplate.queryForList(
                "SELECT context_code FROM profile_health_contexts WHERE user_id = ? ORDER BY context_code",
                userId
            )
        );
        data.put(
            "consents",
            jdbcTemplate.queryForList(
                """
                SELECT consent_type, granted, policy_version, recorded_at
                FROM consent_records
                WHERE user_id = ?
                ORDER BY recorded_at
                """,
                userId
            )
        );

        data.put(
            "meals",
            jdbcTemplate.queryForList(
                """
                SELECT id, meal_date, meal_type, quantity_grams, portion_count,
                       food_name_snapshot, portion_label_snapshot,
                       source_code_snapshot, source_food_ref_snapshot,
                       nutrient_status_snapshot,
                       dietary_classification_snapshot,
                       created_at, updated_at
                FROM meal_entries
                WHERE user_id = ?
                ORDER BY meal_date, created_at
                """,
                userId
            )
        );
        data.put(
            "mealNutrients",
            jdbcTemplate.queryForList(
                """
                SELECT n.meal_entry_id, n.nutrient_code, n.amount, n.unit
                FROM meal_entry_nutrients n
                JOIN meal_entries m ON m.id = n.meal_entry_id
                WHERE m.user_id = ?
                ORDER BY n.meal_entry_id, n.nutrient_code
                """,
                userId
            )
        );
        data.put(
            "mealAllergenSnapshots",
            jdbcTemplate.queryForList(
                """
                SELECT a.meal_entry_id, a.allergen_code
                FROM meal_entry_allergens a
                JOIN meal_entries m ON m.id = a.meal_entry_id
                WHERE m.user_id = ?
                ORDER BY a.meal_entry_id, a.allergen_code
                """,
                userId
            )
        );
        data.put(
            "foodFavorites",
            jdbcTemplate.queryForList(
                """
                SELECT f.slug, f.canonical_name, fav.created_at
                FROM user_food_favorites fav
                JOIN foods f ON f.id = fav.food_id
                WHERE fav.user_id = ?
                ORDER BY fav.created_at
                """,
                userId
            )
        );

        data.put(
            "dietPlans",
            jdbcTemplate.queryForList(
                """
                SELECT id, plan_date, status, generation_mode, engine_status,
                       source_rule_code, source_rule_version,
                       created_at, updated_at
                FROM diet_plans
                WHERE user_id = ?
                ORDER BY plan_date, created_at
                """,
                userId
            )
        );
        data.put(
            "dietPlanItems",
            jdbcTemplate.queryForList(
                """
                SELECT i.id, i.plan_id, i.meal_type, i.display_order,
                       i.food_name_snapshot, i.dietary_classification_snapshot,
                       i.portion_label_snapshot, i.quantity_grams,
                       i.nutrient_focus_code, i.reason_code, i.explanation,
                       i.source_code_snapshot, i.source_food_ref_snapshot
                FROM diet_plan_items i
                JOIN diet_plans p ON p.id = i.plan_id
                WHERE p.user_id = ?
                ORDER BY i.plan_id, i.display_order
                """,
                userId
            )
        );
        data.put(
            "dietPlanNutrients",
            jdbcTemplate.queryForList(
                """
                SELECT n.plan_item_id, n.nutrient_code, n.amount, n.unit
                FROM diet_plan_item_nutrients n
                JOIN diet_plan_items i ON i.id = n.plan_item_id
                JOIN diet_plans p ON p.id = i.plan_id
                WHERE p.user_id = ?
                ORDER BY n.plan_item_id, n.nutrient_code
                """,
                userId
            )
        );

        data.put(
            "healthRecords",
            jdbcTemplate.queryForList(
                """
                SELECT id, record_type, title, summary_text, clinical_date,
                       provider_name, facility_name, source_type, source_system,
                       source_record_ref, interoperability_resource_type,
                       verification_status, provenance_label,
                       source_payload_hash, imported_at, created_at
                FROM health_records
                WHERE user_id = ?
                ORDER BY clinical_date, created_at
                """,
                userId
            )
        );
        data.put(
            "healthObservations",
            jdbcTemplate.queryForList(
                """
                SELECT o.health_record_id, o.observation_code, o.coding_system,
                       o.display_name, o.value_numeric, o.value_text, o.unit,
                       o.reference_range_text, o.observed_at,
                       o.source_observation_ref
                FROM health_record_observations o
                JOIN health_records r ON r.id = o.health_record_id
                WHERE r.user_id = ?
                ORDER BY o.health_record_id, o.observed_at
                """,
                userId
            )
        );
        data.put(
            "healthIntegrations",
            jdbcTemplate.queryForList(
                """
                SELECT provider_code, display_name, integration_mode, status,
                       live_connectivity, interoperability_standard,
                       external_subject_ref, connected_at, disconnected_at,
                       last_imported_at, updated_at
                FROM health_integrations
                WHERE user_id = ?
                ORDER BY provider_code
                """,
                userId
            )
        );

        data.put(
            "nudges",
            jdbcTemplate.queryForList(
                """
                SELECT rule_code, rule_version, category, severity, status,
                       title, message, action_label, action_href, reason_code,
                       source_type, source_ref, evidence_label, evidence_url,
                       first_generated_at, last_evaluated_at, snoozed_until,
                       acknowledged_at, dismissed_at, resolved_at
                FROM nudge_instances
                WHERE user_id = ?
                ORDER BY first_generated_at
                """,
                userId
            )
        );
        data.put(
            "wellnessGoals",
            jdbcTemplate.queryForList(
                """
                SELECT goal_code, target_value, status, started_on, ended_on,
                       created_at, updated_at
                FROM user_wellness_goals
                WHERE user_id = ?
                ORDER BY created_at
                """,
                userId
            )
        );
        data.put(
            "achievements",
            jdbcTemplate.queryForList(
                """
                SELECT achievement_code, earned_at, evidence_value
                FROM user_achievements
                WHERE user_id = ?
                ORDER BY earned_at
                """,
                userId
            )
        );
        data.put(
            "researchFeatureEvents",
            jdbcTemplate.queryForList(
                """
                SELECT event_code, event_version, event_date, occurred_at
                FROM research_feature_events
                WHERE user_id = ?
                ORDER BY event_date, event_code
                """,
                userId
            )
        );
        data.put(
            "securityEvents",
            jdbcTemplate.queryForList(
                """
                SELECT event_type, event_outcome, subject,
                       occurred_at, metadata_json
                FROM security_audit_events
                WHERE user_id = ?
                ORDER BY occurred_at
                """,
                userId
            )
        );

        auditService.record(
            user,
            "ACCOUNT_DATA_EXPORTED",
            "SUCCESS",
            "self-export",
            null
        );

        return data;
    }

    @Transactional
    public void delete(UUID userId, String password) {
        var user = userRepository.findById(userId)
            .orElseThrow(() -> new BadCredentialsException(
                "Account not found."
            ));

        if (user.getRole() != UserRole.USER) {
            throw new IllegalStateException(
                "Staff accounts cannot be self-deleted because operational review history must remain attributable."
            );
        }

        if (!passwordEncoder.matches(
            password,
            user.getPasswordHash()
        )) {
            throw new BadCredentialsException(
                "Password confirmation did not match."
            );
        }

        auditService.record(
            user,
            "ACCOUNT_DELETION_REQUESTED",
            "SUCCESS",
            "self-delete",
            null
        );

        jdbcTemplate.update(
            """
            UPDATE security_audit_events
            SET subject = 'deleted-account',
                metadata_json = NULL
            WHERE user_id = ?
               OR lower(subject) = lower(?)
               OR subject = ?
            """,
            userId,
            user.getEmail(),
            "email-sha256:" + sha256(user.getEmail())
        );

        userRepository.delete(user);
        userRepository.flush();
    }

    private String sha256(String value) {
        try {
            var digest = java.security.MessageDigest.getInstance("SHA-256");
            var bytes = digest.digest(
                value.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );
            return java.util.HexFormat.of().formatHex(bytes);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                "SHA-256 is unavailable.",
                exception
            );
        }
    }
}
