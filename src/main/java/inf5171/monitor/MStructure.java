package inf5171.monitor;

import java.util.List;

/**
 * Created by david on 2016-12-11.
 */
public interface MStructure<T> {
    /**
     * Obtient le contenu de la premiere cellule, mais suspend si vide
     * jusqu’a ce qu'un element soit ajouté
     *
     * @ensure La premiere cellule n'est pas vide
     *
     * @return La valeur qui etait dans la premiere cellule
     */
    T shift();

    /**
     * Ajoute a la fin
     *
     *
     * @param v Valeur a ecrire
     */
    Boolean push(T v );

    /**
     * Ajoute une liste d'elements
     *
     * @param list liste d'elements a ajouter
     */
    Boolean push(List<T> list);

    /**
     *
     * @return le nombre d'elements dans la monitor
     */
    int size();

    void setCompleted(Boolean completed);

    Boolean getCompleted();
}
