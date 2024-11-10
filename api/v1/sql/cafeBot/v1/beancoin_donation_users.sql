CREATE TABLE beancoin_donation_users
(
    user_id BIGINT,
    time_until_next_donation TIMESTAMP
    UNIQUE (user_id)
);
