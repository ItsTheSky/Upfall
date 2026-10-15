package net.itsthesky.upfall.content.reliquary;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.network.Filterable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.WrittenBookContent;

import java.util.ArrayList;
import java.util.List;

public final class ReliquaryBooks {
    private ReliquaryBooks() {}

    public record Book(String id, String title, String author, List<String> pages, List<String> pagesFr) {
        public String pageKey(int page) {
            return "book.upfall.tilted_reliquary." + id + "." + (page + 1);
        }

        public WrittenBookContent content() {
            List<Filterable<Component>> content = new ArrayList<>();
            for (int i = 0; i < pages.size(); i++) {
                content.add(Filterable.passThrough(Component.translatable(pageKey(i))));
            }
            return new WrittenBookContent(Filterable.passThrough(title), author, 0, content, true);
        }

        public ItemStack create() {
            ItemStack stack = new ItemStack(Items.WRITTEN_BOOK);
            stack.set(DataComponents.WRITTEN_BOOK_CONTENT, content());
            return stack;
        }
    }

    public static final Book SURVEYOR_JOURNAL = new Book("surveyor_journal", "Field Journal", "Surveyor Maren", List.of(
            "Third survey, first day of the Fold. We stack space like folded linen and store the weight of the world in lattice vaults. A cube holds what a mountain cannot.",
            "The sky builders lent us their pearlstone and their patience. They ask only that we never fold a city. We have promised. Promises weigh little here.",
            "The Fold pushes. Every vault we close shoves the land a little farther off. The islands drift from us, or we from them. The warden says it is the same thing.",
            "Tomorrow we attempt the Tilt: the whole reliquary turned an eighth of a circle, so the weight runs down the diagonal. Forgive the shaking of my hand."),
            List.of(
                    "Troisième relevé, premier jour du Pli. Nous empilons l'espace comme du lin plié et rangeons le poids du monde dans des caveaux en treillis. Un cube contient ce qu'une montagne ne peut pas.",
                    "Les bâtisseurs du ciel nous ont prêté leur pearlstone et leur patience. Ils ne demandent qu'une chose : ne jamais plier une cité. Nous l'avons promis. Les promesses pèsent peu ici.",
                    "Le Pli repousse. Chaque caveau que nous fermons éloigne un peu la terre. Les îles dérivent loin de nous, ou nous d'elles. Le gardien dit que c'est pareil.",
                    "Demain, nous tentons la Bascule : le reliquaire entier tourné d'un huitième de cercle, pour que le poids coule le long de la diagonale. Pardonnez le tremblement de ma main."));

    public static final Book HOROLOGIST_LETTER = new Book("horologist_letter", "To the Spire", "The Surveyors of the Fold", List.of(
            "To the Horologists of the Spire. Your last shipment of brass arrived intact; our thanks. Our vaults keep time poorly, but you have taught us to count it.",
            "You asked what we store. Weight, friends. The weight of falling. We fold a room into a point and set the point in a cube, and the room forgets it was ever heavy.",
            "Should our work go wrong, follow the lattice beams; we laid them toward the nearest archive. Trust them more than your eyes.",
            "Send no more brass. Send a clock tuned to a stranger day. We would like to know how long a Tilt lasts."),
            List.of(
                    "Aux Horlogers de la Flèche. Votre dernier envoi de laiton est arrivé intact ; merci. Nos caveaux gardent mal le temps, mais vous nous avez appris à le compter.",
                    "Vous demandiez ce que nous rangeons. Du poids, amis. Le poids de la chute. Nous plions une pièce en un point et posons le point dans un cube, et la pièce oublie qu'elle fut lourde.",
                    "Si notre travail tourne mal, suivez les poutres de treillis ; nous les avons posées vers l'archive la plus proche. Fiez-vous à elles plus qu'à vos yeux.",
                    "N'envoyez plus de laiton. Envoyez une horloge réglée sur un jour plus étrange. Nous aimerions savoir combien de temps dure une Bascule."));

    public static final Book WARDEN_NOTE = new Book("warden_note", "The Last Note", "The Fold Warden", List.of(
            "The Tilt held for one breath. Then every vault shattered mid-fold and stopped, tilted, exactly as you see them. Nothing fell. Nothing could.",
            "The Fold pushed us out beyond every island, which is why none of you came. The core still hums. It opens only for what made it: four Fold Shards.",
            "One shard rests in each satellite that circles the heart. Set all four in the sockets of the sealed walls before you approach the heart. A socket cannot be emptied again.",
            "When the fourth sits true, the seals will dissolve and the heart will stand open. Within lies our last tool, the Fold Anchor. It remembers a place for half a minute.",
            "Anchor yourself, step where you must, and the Fold will bring you back. Use it well. Do not fold a city."),
            List.of(
                    "La Bascule a tenu le temps d'un souffle. Puis chaque caveau s'est brisé en plein pli et s'est figé, incliné, tel que vous les voyez. Rien n'est tombé. Rien ne le pouvait.",
                    "Le Pli nous a repoussés au-delà de toutes les îles, voilà pourquoi aucun de vous n'est venu. Le noyau bourdonne encore. Il ne s'ouvre que pour ce qui l'a fait : quatre Éclats du Pli.",
                    "Un éclat repose dans chaque satellite qui orbite le cœur. Placez les quatre dans les réceptacles des murs scellés avant d'approcher le cœur. Un réceptacle ne se vide plus.",
                    "Quand le quatrième sera en place, les sceaux se dissoudront et le cœur s'ouvrira. Au centre repose notre dernier outil, l'Ancre du Pli. Elle garde un lieu en mémoire une demi-minute.",
                    "Ancrez-vous, allez où vous devez, et le Pli vous ramènera. Servez-vous-en bien. Ne pliez pas une cité."));

    public static List<Book> all() {
        return List.of(SURVEYOR_JOURNAL, HOROLOGIST_LETTER, WARDEN_NOTE);
    }
}
