package net.tagtart.rechantment.networking.data;

public enum PurchaseBookResultCase {

    // Book can be purchased
    SUCCESS,

    // Not enough room inventory for a book
    INVENTORY_FULL,

    // Not enough EXP for a given tier
    INSUFFICIENT_EXP,

    // Not enough bookshelves for a given tier
    INSUFFICIENT_BOOKS,

    // Not enough of required floor for a given tier
    INSUFFICIENT_FLOOR,

    // Not enough of required lapis for a given tier
    INSUFFICIENT_LAPIS
}
